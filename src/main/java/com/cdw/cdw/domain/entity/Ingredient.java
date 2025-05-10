package com.cdw.cdw.domain.entity;

import com.cdw.cdw.domain.enums.BaseUnit;
import jakarta.persistence.*;
import lombok.Data;
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

    /**
     * Mối quan hệ một-nhiều với IngredientBatch (Phía nghịch đảo - Inverse Side).
     * Một loại Ingredient có thể có nhiều lô hàng (batch).
     * mappedBy = "ingredient": Được quản lý bởi thuộc tính 'ingredient' trong entity IngredientBatch.
     * cascade = CascadeType.ALL, orphanRemoval = true: Nếu xóa một loại Ingredient (hiếm khi xảy ra),
     * thì tất cả các lô hàng liên quan cũng sẽ bị xóa theo. Cân nhắc kỹ logic này.
     */
//    @OneToMany(mappedBy = "ingredient", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<IngredientBatch> batches = new HashSet<>(); // Nên khởi tạo collection

    /**
     * Mối quan hệ một-nhiều với MenuItemIngredient (Phía nghịch đảo - Inverse Side).
     * Một Ingredient có thể được sử dụng trong nhiều công thức món ăn.
     * mappedBy = "ingredient": Được quản lý bởi thuộc tính 'ingredient' trong entity MenuItemIngredient.
     * Thường không cần cascade từ Ingredient, việc xóa ingredient đang dùng trong công thức
     * cần được xử lý ở tầng service (ví dụ: không cho xóa hoặc cập nhật công thức).
     */
    @OneToMany(mappedBy = "ingredient", fetch = FetchType.LAZY)
    private Set<MenuItemIngredient> menuItemIngredients = new HashSet<>(); // Nên khởi tạo collection

}
