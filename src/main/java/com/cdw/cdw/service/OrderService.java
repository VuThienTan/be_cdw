package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.OrdersCreateRequest;
import com.cdw.cdw.domain.dto.request.OrdersItemCreateRequest;
import com.cdw.cdw.domain.dto.response.OrderCreateResponse;
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
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    MenuItemIngredientRepositoy menuItemIngredientRepository;

    public List<OrderResponse> getAllOrders() {
        List<Orders> orders = ordersRepository.findAll();
        return orders.stream()
                .map(ordersMapper::toOrderResponse)
                .collect(Collectors.toList());
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

            // Get ingredients required for this menu item
            List<MenuItemIngredient> menuItemIngredients = menuItemIngredientRepository.findByMenuItem_Id(itemRequest.getMenuItemId());
            
            // Deduct ingredients from inventory (nearest expiry first)
            for (MenuItemIngredient menuItemIngredient : menuItemIngredients) {
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
