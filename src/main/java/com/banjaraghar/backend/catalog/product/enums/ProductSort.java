package com.banjaraghar.backend.catalog.product.enums;

import org.springframework.data.domain.Sort;

public enum ProductSort {

    NEWEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    PRICE_LOW_TO_HIGH(Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.DESC, "createdAt"))),
    PRICE_HIGH_TO_LOW(Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.DESC, "createdAt"))),
    // Sales data only exists after Step 6 (orders); until then this behaves like NEWEST
    BEST_SELLERS(Sort.by(Sort.Direction.DESC, "createdAt"));

    private final Sort sort;

    ProductSort(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
