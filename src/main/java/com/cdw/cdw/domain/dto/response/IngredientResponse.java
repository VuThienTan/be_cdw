package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.enums.BaseUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponse {
    private Integer id;
    private String name;
    private BaseUnit baseUnit;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 