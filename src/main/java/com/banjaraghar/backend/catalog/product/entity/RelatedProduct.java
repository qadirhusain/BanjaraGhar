package com.banjaraghar.backend.catalog.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "related_products")
@Getter
@Setter
@NoArgsConstructor
public class RelatedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "related_product_id", nullable = false)
    private Product relatedProduct;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
