package com.cdw.cdw.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "categorys")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id")
    String id;

    @Column(name = "name")
    String name;

    @Lob
    @Column(name = "description")
    String description;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @Column(name = "parent_category_id")
//    Category parentCategoryId;
//
//    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
//    private Set<Category> subCategories = new HashSet<>();
//
//    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
//    private Set<MenuItem> menuItems = new HashSet<>();
//
//    @OneToMany(mappedBy = "menuItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<MenuItemIngredient> menuItemIngredients = new HashSet<>(); // Nên khởi tạo collection

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
