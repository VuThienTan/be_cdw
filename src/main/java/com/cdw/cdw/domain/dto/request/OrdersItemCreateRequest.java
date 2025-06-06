package com.cdw.cdw.domain.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersItemCreateRequest {
    Long menuItemId;
    int quantity;
    BigDecimal price;
}
