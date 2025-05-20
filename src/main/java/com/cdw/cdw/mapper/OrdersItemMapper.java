package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.OrdersItemCreateRequest;
import com.cdw.cdw.domain.entity.OrdersItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdersItemMapper {
    OrdersItem toOrderItem(OrdersItemCreateRequest ordersItem);

}
