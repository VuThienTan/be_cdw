package com.cdw.cdw.domain.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
    public class StockInRequest {
        private Integer ingredientId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String supplier;
        private String note;
        private LocalDate expiryDate;
    }
