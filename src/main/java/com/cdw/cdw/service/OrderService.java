package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.OrdersCreateRequest;
import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.exception.ErrorCode;
import com.cdw.cdw.repository.MenuItemRepository;
import com.cdw.cdw.repository.OrdersRepository;
import com.cdw.cdw.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrdersRepository orderRepository;

    public void createOrder(OrdersCreateRequest ordersCreateRequest) {
        User user = userRepository.findById(ordersCreateRequest.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));

    }
}
