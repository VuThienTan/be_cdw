package com.cdw.cdw.domain.entity;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long productId;
    private String name;
    private double price;
    private int quantity;
    private String image;
}