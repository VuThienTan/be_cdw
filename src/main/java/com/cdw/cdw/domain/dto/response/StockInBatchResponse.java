package com.cdw.cdw.domain.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StockInBatchResponse {
    private Integer batchId;
    private Integer ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private BigDecimal used;
    private BigDecimal remainingQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String supplier;
    private String note;
    private LocalDate expiryDate;
    private LocalDateTime importedAt;
} 