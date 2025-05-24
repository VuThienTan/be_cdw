package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.response.CartItemResponse;
import com.cdw.cdw.domain.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartItemMapper {

    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .menuItemId(cartItem.getMenuItem().getId())
                .menuItemName(cartItem.getMenuItem().getName())
                .menuItemImageUrl(cartItem.getMenuItem().getImageUrl())
                .price(cartItem.getMenuItem().getPrice())
                .discount(cartItem.getMenuItem().getDiscount())
                .quantity(cartItem.getQuantity())
                .note(cartItem.getNote())
                .addedAt(cartItem.getCreatedAt())
                .build();
    }

    public List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }
}
