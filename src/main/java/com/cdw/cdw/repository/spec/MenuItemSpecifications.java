package com.cdw.cdw.repository.spec;


import com.cdw.cdw.domain.entity.MenuItem;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public class MenuItemSpecifications {

    public static Specification<MenuItem> hasKeyword(String keyword) {
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



    public static Specification<MenuItem> hasRation(Integer ration) {
        return (root, query, cb) -> {
            if (ration == null) return null;
            return cb.equal(root.get("ration"), ration);
        };
    }

    public static Specification<MenuItem> hasPriceBetween(Double priceFrom, Double priceTo) {
        return (root, query, cb) -> {
            if (priceFrom == null || priceTo == null) return null;
            return cb.between(root.get("price"), priceFrom, priceTo);
        };
    }
}