package com.banjaraghar.backend.catalog.product.entity;

import com.banjaraghar.backend.catalog.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Selling price in paise. */
    @Column(nullable = false)
    private long price;

    /** Original (struck-through) price in paise. */
    @Column(nullable = false)
    private long mrp;

    private String material;

    private String colour;

    private String size;

    @Column(name = "care_instructions")
    private String careInstructions;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private String sku;

    @Column(name = "packed_weight_g", nullable = false)
    private int packedWeightG;

    @Column(name = "packed_length_cm", nullable = false)
    private int packedLengthCm;

    @Column(name = "packed_breadth_cm", nullable = false)
    private int packedBreadthCm;

    @Column(name = "packed_height_cm", nullable = false)
    private int packedHeightCm;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
