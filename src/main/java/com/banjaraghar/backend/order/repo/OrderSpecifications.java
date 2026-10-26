package com.banjaraghar.backend.order.repo;

import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    /** Admin order list: optional status, and an optional search on the order number or the customer's email. */
    public static Specification<Order> forAdmin(OrderStatus status, String q) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderNumber")), pattern),
                        cb.like(cb.lower(root.get("user").get("email")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}