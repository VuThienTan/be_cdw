package com.cdw.cdw.domain.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersCreateRequest {
    String phoneNumber;
    String userId;
    String address;
    List<OrdersItemCreateRequest> orderItem;
    double latitude;
    double longitude;
    String note;
    String paymentMethod;
    String promoCode;
}
