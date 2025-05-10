package com.cdw.cdw.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table (name = "menu_item")
@FieldDefaults (level = AccessLevel.PRIVATE)
public class MenuItem {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "menu_itenm_id")
    Long id;

    @Column(name = "name")
    String name;

    @Column(name = "description")
    String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "is_available", nullable = false) // Cột 'is_available', không null
    private boolean available = true; // Giá trị mặc định khi tạo đối tượng Java

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false) // Không null, không cập nhật
    private LocalDateTime createdAt;

    @UpdateTimestamp // Tự động gán thời gian cập nhật
    @Column(name = "updated_at", nullable = false) // Không null
    private LocalDateTime updatedAt;

}
