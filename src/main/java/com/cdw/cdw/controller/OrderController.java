package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.OrdersCreateRequest;
import com.cdw.cdw.domain.dto.response.OrderCreateResponse;
import com.cdw.cdw.domain.dto.response.OrderResponse;
import com.cdw.cdw.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrdersCreateRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.checkout(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Object> getAllOrders() {
        return ApiResponse.<Object>builder()
                .result(orderService.getAllOrders())
                .build();
    }
}
