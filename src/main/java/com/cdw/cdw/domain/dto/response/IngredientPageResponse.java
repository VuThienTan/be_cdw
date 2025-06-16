package com.cdw.cdw.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientPageResponse {
    private List<IngredientResponse> ingredients;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
} 