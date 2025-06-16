package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.OrdersCreateRequest;
import com.cdw.cdw.domain.dto.response.OrderCreateResponse;
import com.cdw.cdw.domain.dto.response.OrderDetailResponse;
import com.cdw.cdw.domain.dto.response.OrderPageResponse;
import com.cdw.cdw.domain.dto.response.OrderResponse;
import com.cdw.cdw.domain.enums.OrderStatus;
import com.cdw.cdw.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
    public ApiResponse<OrderPageResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        return ApiResponse.<OrderPageResponse>builder()
                .result(orderService.getOrdersWithPagination(page, size, sortBy, direction, status, userId, phoneNumber, fromDate, toDate))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderById(@PathVariable Long id) {
        return ApiResponse.<OrderDetailResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }
}
