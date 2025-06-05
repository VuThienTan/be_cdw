package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.response.CartItemResponse;
import com.cdw.cdw.domain.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "menuItem.id", target = "menuItemId")
    @Mapping(source = "menuItem.name", target = "menuItemName")
    @Mapping(source = "menuItem.imageUrl", target = "menuItemImageUrl")
    @Mapping(source = "menuItem.price", target = "price")
    @Mapping(source = "menuItem.discount", target = "discount")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "createdAt", target = "addedAt")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);
}
