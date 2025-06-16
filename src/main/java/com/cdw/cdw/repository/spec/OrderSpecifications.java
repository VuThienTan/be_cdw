package com.cdw.cdw.repository.spec;

import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.enums.OrderStatus;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecifications {

    public static Specification<Orders> hasStatus(OrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Orders> hasUserId(String userId) {
        return (root, query, cb) -> {
            if (userId == null || userId.isBlank()) return null;
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Orders> hasPhoneNumber(String phoneNumber) {
        return (root, query, cb) -> {
            if (phoneNumber == null || phoneNumber.isBlank()) return null;
            String lowerPhoneNumber = phoneNumber.toLowerCase();
            Expression<String> phoneField = cb.lower(root.get("phoneNumber"));
            return cb.like(phoneField, "%" + lowerPhoneNumber + "%");
        };
    }

    public static Specification<Orders> hasDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return (root, query, cb) -> {
            if (fromDate == null && toDate == null) return null;
            if (fromDate != null && toDate != null) {
                return cb.between(root.get("createdAt"), fromDate, toDate);
            } else if (fromDate != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
            } else {
                return cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
            }
        };
    }
} 