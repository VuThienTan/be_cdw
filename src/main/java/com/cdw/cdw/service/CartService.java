package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.CartItemRequest;
import com.cdw.cdw.domain.dto.response.CartItemResponse;
import com.cdw.cdw.domain.dto.response.CartResponse;
import com.cdw.cdw.domain.entity.CartItem;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
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


    @Transactional
    public CartItemResponse addItemToCart(String userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() ->  AppException.notFound("menu.item.not.found"));

        // Kiểm tra xem sản phẩm có sẵn không
        if (!menuItem.isAvailable()) {
            throw AppException.badRequest("menuItem.not.available");
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
// giá chưa giảm
    private BigDecimal calculateSubtotal(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
//giảm giá
    private BigDecimal calculateTotalDiscount(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.getDiscount() != null ?
                        item.getDiscount().multiply(BigDecimal.valueOf(item.getQuantity())) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    public CartResponse getCurrentUserCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        return getCartByUserId(user.getId());
    }

    @Transactional
    public CartItemResponse addItemToCartForCurrentUser(CartItemRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw AppException.unauthorized("unauthenticated");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return  AppException.notFound("user.not.found");
                });
        return addItemToCart(user.getId(), request);
    }


//    Xóa sản phẩm khỏi giỏ hàng
    @Transactional
    public void removeCartItem(Long cartItemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> AppException.notFound("cartItem.not.found"));

        // Kiểm tra xem sản phẩm có thuộc về người dùng hiện tại không
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw AppException.forbidden("access.denied");
        }

        cartItemRepository.delete(cartItem);
    }

    // Cập nhật số lượng
    @Transactional
    public CartItemResponse updateCartItemQuantity(Long id, int quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("cartItem.not.found"));

        // Kiểm tra xem sản phẩm có thuộc về người dùng hiện tại không
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw AppException.forbidden("access.denied");
        }

        // Cập nhật số lượng
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);

        return cartItemMapper.toCartItemResponse(cartItem);
    }

    @CacheEvict(value = "userCart", key = "#userId")
    public void clearCart(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        cartItemRepository.deleteAll(cartItems);
    }

}
