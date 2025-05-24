package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.CartItemRequest;
import com.cdw.cdw.domain.dto.request.CartItemUpdateRequest;
import com.cdw.cdw.domain.dto.response.CartItemResponse;
import com.cdw.cdw.domain.dto.response.CartResponse;
import com.cdw.cdw.domain.entity.CartItem;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.exception.ErrorCode;
import com.cdw.cdw.mapper.CartItemMapper;
import com.cdw.cdw.repository.CartItemRepository;
import com.cdw.cdw.repository.MenuItemRepository;
import com.cdw.cdw.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {
    CartItemRepository cartItemRepository;
    MenuItemRepository menuItemRepository;
    UserRepository userRepository;
    CartItemMapper cartItemMapper;

    @Cacheable(value = "userCart", key = "#userId")
    public CartResponse getCartByUserId(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> cartItemResponses = cartItemMapper.toCartItemResponseList(cartItems);

        BigDecimal subtotal = calculateSubtotal(cartItemResponses);
        BigDecimal totalDiscount = calculateTotalDiscount(cartItemResponses);
        BigDecimal total = subtotal.subtract(totalDiscount);



        return CartResponse.builder()
                .items(cartItemResponses)
                .totalItems(cartItems.size())
                .subtotal(subtotal)
                .totalDiscount(totalDiscount)
                .total(total)
                .build();
    }

    private BigDecimal calculateSubtotal(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalDiscount(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.getDiscount() != null ?
                        item.getDiscount().multiply(BigDecimal.valueOf(item.getQuantity())) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#userId")
    public CartItemResponse addItemToCart(String userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));

        // Kiểm tra xem sản phẩm có sẵn không
        if (!menuItem.isAvailable()) {
            throw new AppException(ErrorCode.MENU_ITEM_NOT_AVAILABLE);
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndMenuItem(user, menuItem);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // Nếu đã có, tăng số lượng
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            if (request.getNote() != null && !request.getNote().isEmpty()) {
                cartItem.setNote(request.getNote());
            }
        } else {
            // Nếu chưa có, tạo mới
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setNote(request.getNote());
        }

        cartItem = cartItemRepository.save(cartItem);

        return cartItemMapper.toCartItemResponse(cartItem);
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#userId")
    public CartItemResponse updateCartItem(String userId, Long cartItemId, CartItemUpdateRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Kiểm tra xem người dùng có quyền cập nhật mục này không
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        cartItem.setQuantity(request.getQuantity());
        if (request.getNote() != null) {
            cartItem.setNote(request.getNote());
        }

        cartItem = cartItemRepository.save(cartItem);

        return cartItemMapper.toCartItemResponse(cartItem);
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#userId")
    public void removeCartItem(String userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Kiểm tra xem người dùng có quyền xóa mục này không
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#userId")
    public void clearCart(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        cartItemRepository.deleteAll(cartItems);
    }

    public CartResponse getCurrentUserCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return getCartByUserId(user.getId());
    }

    // Scheduled task to clean old cart items (runs every day at midnight)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldCartItems() {
        log.info("Starting scheduled cleanup of old cart items");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<CartItem> oldItems = cartItemRepository.findOldCartItems(user.getId(), 30);
            if (!oldItems.isEmpty()) {
                log.info("Removing {} old cart items for user {}", oldItems.size(), user.getId());
                cartItemRepository.deleteAll(oldItems);
            }
        }

        log.info("Completed scheduled cleanup of old cart items");
    }
    @Transactional
    public CartItemResponse addItemToCartForCurrentUser(CartItemRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });
        return addItemToCart(user.getId(), request);
    }


}
