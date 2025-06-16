package com.cdw.cdw.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemUpdateRequest {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer ration;
    private String imageUrl;
    private List<IngredientRequest> ingredients;
} 