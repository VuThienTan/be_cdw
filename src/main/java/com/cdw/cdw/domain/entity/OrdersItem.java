package com.cdw.cdw.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_items") // tránh trùng tên và rõ ràng hơn
public class OrdersItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    MenuItem menuItem;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    int quantity;

    @Transient
    public BigDecimal getTotalPrice() {
        return unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    }
}
