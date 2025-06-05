package com.cdw.cdw.domain.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreateResponse {
    String orderId;
    String userId;
    String phoneNumber;
    String address;
    BigDecimal latitude;
    BigDecimal longitude;
    List<OrderItemResponse> orderItems;
    BigDecimal totalPrice;
    String paymentMethod;
    String status;
    String promoCode;
    LocalDateTime createdAt;
}
