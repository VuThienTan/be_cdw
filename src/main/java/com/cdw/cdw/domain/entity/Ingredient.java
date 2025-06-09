package com.cdw.cdw.domain.entity;

import com.cdw.cdw.domain.enums.BaseUnit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "ingredient")
public class Ingredient {
    @Id // Đánh dấu đây là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Sử dụng chiến lược tự tăng của DB
    @Column(name = "ingredient_id") // Ánh xạ tới cột 'ingredient_id'
    private Integer id;

    @Column(name = "name", length = 255, nullable = false, unique = true) // Cột 'name', không null, duy nhất
    private String name;

    @Column(name = "base_unit", nullable = false)
    @Enumerated(EnumType.STRING)
    private BaseUnit baseUnit;

    @Lob
    @Column(name = "description")
    private String description;

    @CreationTimestamp // Tự động gán thời gian tạo mới
    @Column(name = "created_at", nullable = false, updatable = false) // Không null, không cập nhật
    private LocalDateTime createdAt;

    @UpdateTimestamp // Tự động gán thời gian cập nhật
    @Column(name = "updated_at", nullable = false) // Không null
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ingredient", fetch = FetchType.LAZY)
    private Set<MenuItemIngredient> menuItemIngredients = new HashSet<>(); // Nên khởi tạo collection

    @OneToOne(mappedBy = "ingredient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private Set<StockInBatch> stockInBatches = new HashSet<>();

}
