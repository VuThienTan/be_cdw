package com.cdw.cdw.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String type; // VD: LOW_STOCK, NEW_ORDER

    @Column(nullable = false, length = 1000)
    String message;

    @Lob
    String data; // JSON string chứa thông tin chi tiết

    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "is_read")

    private boolean read = false;

} 