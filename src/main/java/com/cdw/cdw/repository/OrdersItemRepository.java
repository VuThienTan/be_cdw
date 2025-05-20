package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.OrdersItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersItemRepository extends JpaRepository<OrdersItem, String> {
}
