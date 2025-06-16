package com.cdw.cdw.repository.spec;

import com.cdw.cdw.domain.entity.Ingredient;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.enums.BaseUnit;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public class IngredientSpecifications {

    public static Specification<Ingredient> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String lowerKeyword = keyword.toLowerCase();
            Expression<String> nameField = cb.lower(root.get("name"));
            return cb.or(
                    cb.like(nameField, lowerKeyword + "%"),
                    cb.like(nameField, "% " + lowerKeyword + "%")
            );
        };
    }


    public static Specification<Ingredient> hasBaseUnit(BaseUnit baseUnit) {
        return (root, query, cb) -> {
            if (baseUnit == null) return null;
            return cb.equal(root.get("baseUnit"), baseUnit);
        };
    }
} 