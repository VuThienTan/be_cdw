package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.OrdersCreateRequest;
import com.cdw.cdw.domain.dto.request.OrdersItemCreateRequest;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.entity.OrdersItem;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.domain.enums.OrderStatus;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.exception.ErrorCode;
import com.cdw.cdw.mapper.OrdersItemMapper;
import com.cdw.cdw.repository.MenuItemRepository;
import com.cdw.cdw.repository.OrdersItemRepository;
import com.cdw.cdw.repository.OrdersRepository;
import com.cdw.cdw.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {
    UserRepository userRepository;
    MenuItemRepository menuItemRepository;
    OrdersRepository orderRepository;
    OrdersItemRepository orderItemsRepository;
    OrdersItemMapper ordersItemMapper;

    public void createOrder(OrdersCreateRequest ordersCreateRequest) {
        log.info("Create Order" + ordersCreateRequest.toString());
        User user = userRepository.findById(ordersCreateRequest.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));
        Orders order = Orders.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .build();

        List<OrdersItem> orderItems = ordersCreateRequest.getItems().stream()
                .map(itemReq -> {
                    MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                            .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));

                    return OrdersItem.builder()
                            .order(order)
                            .menuItem(menuItem)
                            .quantity(itemReq.getQuantity())
                            .unitPrice(menuItem.getPrice())
                            .build();
                })
                .toList();

        order.setOrderItems(orderItems);
        orderRepository.save(order);
    }
}
