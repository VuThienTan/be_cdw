package com.cdw.cdw.domain.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InfoMenuItemResponse {
    Long id;
    String name;
    String description;
    BigDecimal price;
    BigDecimal discount;
    int ration;
    String imageUrl;
    List<MenuItemIngredientResponse> ingredients;
    Integer inStock;
}
