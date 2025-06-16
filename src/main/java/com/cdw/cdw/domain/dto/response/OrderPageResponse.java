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
public class OrderPageResponse {
    private List<OrderListResponse> content;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
} 