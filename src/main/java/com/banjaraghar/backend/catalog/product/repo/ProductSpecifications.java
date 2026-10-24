package com.banjaraghar.backend.catalog.product.repo;

import com.banjaraghar.backend.catalog.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    /**
     * Public shop filter: only visible products in visible categories, plus the optional filters.
     */
    public static Specification<Product> forShop(UUID categoryId, Long minPrice, Long maxPrice,
                                                 boolean inStockOnly, Collection<UUID> matchingIds) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.isTrue(root.get("category").get("active")));

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (inStockOnly) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }
            if (matchingIds != null) {
                predicates.add(root.get("id").in(matchingIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}