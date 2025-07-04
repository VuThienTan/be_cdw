package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.UpdateUserRequest;
import com.cdw.cdw.domain.dto.response.OrderCreateResponse;
import com.cdw.cdw.domain.dto.response.OrderDetailResponse;
import com.cdw.cdw.domain.dto.response.OrderItemResponse;
import com.cdw.cdw.domain.dto.response.OrderListResponse;
import com.cdw.cdw.domain.dto.response.OrderResponse;
import com.cdw.cdw.domain.dto.response.OrdersResponse;
import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.entity.OrdersItem;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.domain.enums.OrderStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrdersMapper {
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "lat", target = "latitude")
    @Mapping(source = "lng", target = "longitude")
    OrderCreateResponse toOrderCreateResponse(Orders order);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status", qualifiedByName = "orderStatusToString")
    @Mapping(source = "orderItems", target = "items")  // map orderItems sang items
    OrderResponse toOrderResponse(Orders order);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phoneNumber", target = "userPhone")
    @Mapping(source = "user.address", target = "userAddress")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    OrderDetailResponse toOrderDetailResponse(Orders order);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "paymentMethod", target = "paymentMethod")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "promoCode", target = "promoCode")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    OrderListResponse toOrderListResponse(Orders order);

    @Mapping(source = "menuItem.id", target = "menuItemId")
    @Mapping(source = "menuItem.name", target = "menuItemName")
    @Mapping(source = "menuItem.imageUrl", target = "imageUrl")
    @Mapping(target = "totalPrice", expression = "java(item.getTotalPrice())")
    OrderItemResponse toOrdersItemResponse(OrdersItem item);

    List<OrderItemResponse> toOrdersItemResponseList(List<OrdersItem> items);

    @Named("orderStatusToString")
    default String orderStatusToString(OrderStatus status) {
        return status == null ? null : status.name();
    }


}

