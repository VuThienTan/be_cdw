package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.OrdersCreateRequest;
import com.cdw.cdw.domain.dto.request.OrdersItemCreateRequest;
import com.cdw.cdw.domain.dto.response.OrderCreateResponse;
import com.cdw.cdw.domain.dto.response.OrderDetailResponse;
import com.cdw.cdw.domain.dto.response.OrderListResponse;
import com.cdw.cdw.domain.dto.response.OrderPageResponse;
import com.cdw.cdw.domain.dto.response.OrderResponse;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.MenuItemIngredient;
import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.entity.OrdersItem;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.domain.enums.OrderStatus;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.mapper.OrdersMapper;
import com.cdw.cdw.repository.*;
import com.cdw.cdw.repository.spec.OrderSpecifications;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {
    UserRepository userRepository;
    MenuItemRepository menuItemRepository;
    OrdersRepository ordersRepository;
    CartService cartService;
    OrdersMapper ordersMapper;
    EmailService emailService;
    StockInService stockInService;
    MenuItemIngredientRepository menuItemIngredientRepository;

    public List<OrderResponse> getAllOrders() {
        List<Orders> orders = ordersRepository.findAll();
        return orders.stream()
                .map(ordersMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderDetailResponse getOrderById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("order.not.found"));
        return ordersMapper.toOrderDetailResponse(order);
    }

    public OrderPageResponse getOrdersWithPagination(
            int page, 
            int size, 
            String sortBy, 
            String direction,
            OrderStatus status,
            String userId,
            String phoneNumber,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Orders> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.hasUserId(userId))
                .and(OrderSpecifications.hasPhoneNumber(phoneNumber))
                .and(OrderSpecifications.hasDateRange(fromDate, toDate));

        Page<Orders> data = ordersRepository.findAll(spec, pageRequest);

        return OrderPageResponse.builder()
                .content(data.getContent().stream()
                        .map(ordersMapper::toOrderListResponse)
                        .collect(Collectors.toList()))
                .currentPage(data.getNumber())
                .totalPages(data.getTotalPages())
                .totalItems(data.getTotalElements())
                .pageSize(data.getSize())
                .build();
    }

    @Transactional
    public OrderResponse checkout(OrdersCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        Orders order = Orders.builder()
                .user(user)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .lat(BigDecimal.valueOf(request.getLatitude()))
                .lng(BigDecimal.valueOf(request.getLongitude()))
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod())
                .promoCode(request.getPromoCode())
                .status(OrderStatus.PENDING)
                .build();

        // Process each order item and deduct ingredients
        for (OrdersItemCreateRequest itemRequest : request.getOrderItem()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> AppException.notFound("menu.item.not.found"));

            // Get ingredients required for this menu item (with ingredients loaded)
            List<MenuItemIngredient> menuItemIngredients = menuItemIngredientRepository.findByMenuItem_IdWithIngredients(itemRequest.getMenuItemId());
            
            // Deduct ingredients from inventory (nearest expiry first)
            for (MenuItemIngredient menuItemIngredient : menuItemIngredients) {
                // Add null checks
                if (menuItemIngredient.getIngredient() == null) {
                    throw AppException.badRequest("Ingredient data is missing for menu item: " + menuItem.getName());
                }
                
                if (menuItemIngredient.getIngredient().getId() == null) {
                    throw AppException.badRequest("Ingredient ID is missing for ingredient: " + menuItemIngredient.getIngredient().getName());
                }
                
                BigDecimal totalRequiredQuantity = menuItemIngredient.getQuantityRequired()
                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                
                stockInService.deductIngredientsFromNearestExpiry(
                    menuItemIngredient.getIngredient().getId(), 
                    totalRequiredQuantity
                );
            }

            OrdersItem item = new OrdersItem();
            item.setMenuItem(menuItem);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getPrice());

            order.addOrderItem(item);
        }

        Orders savedOrder = ordersRepository.save(order);
        cartService.clearCart(user.getId());
        try {
            emailService.sendInvoiceEmail(user.getEmail(), savedOrder);
        } catch (MessagingException e) {
            throw AppException.serverError("email.sending.error");
        }
        return ordersMapper.toOrderResponse(savedOrder);
    }
}
