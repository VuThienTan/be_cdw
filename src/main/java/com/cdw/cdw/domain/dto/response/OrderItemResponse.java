package com.cdw.cdw.domain.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String menuItemId;
    String menuItemName;
    int quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
}