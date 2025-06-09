package com.cdw.cdw.domain.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
      Integer ingredientId;
      String ingredientName;
      BigDecimal quantity;
      String baseUnit;
      LocalDateTime lastUpdatedAt;
}