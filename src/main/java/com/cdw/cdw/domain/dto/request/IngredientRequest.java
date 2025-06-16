package com.cdw.cdw.domain.dto.request;

import com.cdw.cdw.domain.enums.BaseUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequest {
    private String name;
    private BaseUnit baseUnit;
    private BigDecimal quantityRequired;
} 