package com.cdw.cdw.domain.entity;

import com.cdw.cdw.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "address")
    String address;

    @Column(name = "lat", precision = 10, scale = 6)
    BigDecimal lat;

    @Column(name = "lng", precision = 10, scale = 6)
    BigDecimal lng;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrdersItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    OrderStatus status = OrderStatus.PENDING;

    @Column(name = "payment_method")
    String paymentMethod;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "promo_code")
    String promoCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Transient
    public BigDecimal getTotalPrice() {
        return orderItems.stream()
                .map(OrdersItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addOrderItem(OrdersItem item) {
        item.setOrder(this);
        this.orderItems.add(item);
    }
}
