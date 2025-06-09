package com.cdw.cdw.repository.spec;

import com.cdw.cdw.domain.entity.Inventory;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecifications {
    public static Specification<Inventory> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("ingredient").get("name")), "%" + keyword.toLowerCase() + "%");
        };
    }
}
