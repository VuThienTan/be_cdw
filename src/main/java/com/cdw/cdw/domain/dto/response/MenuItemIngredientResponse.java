package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.enums.BaseUnit;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItemIngredientResponse {
    String name;
    BaseUnit baseUnit;
    BigDecimal quantityRequired;
}
