package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.entity.OrdersItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersResponse {
    String userId;
    List<OrdersItem> orderItems;
}
