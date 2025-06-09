package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItemResponse {
    Long id;
    String name;
    String description;
    BigDecimal price;
    BigDecimal discount;
    int ration;
    String imageUrl;
    Integer inStock;
}
