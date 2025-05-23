package com.cdw.cdw.domain.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Long id;
    Long menuItemId;
    String menuItemName;
    String menuItemImageUrl;
    BigDecimal price;
    BigDecimal discount;
    Integer quantity;
    String note;
    LocalDateTime addedAt;
}
