package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String orderId;
    String userId;
    String phoneNumber;
    String address;
    BigDecimal lat;
    BigDecimal lng;
    String status;
    String paymentMethod;
    String note;
    String promoCode;
    BigDecimal totalPrice;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<OrderItemResponse> items;
}

