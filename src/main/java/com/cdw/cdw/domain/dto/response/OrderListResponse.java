package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderListResponse {
    Long id;
    String userId;
    String userName;
    String userEmail;
    String phoneNumber;
    String address;
    OrderStatus status;
    String paymentMethod;
    String note;
    String promoCode;
    BigDecimal totalPrice;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
} 