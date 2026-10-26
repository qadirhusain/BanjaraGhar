package com.banjaraghar.backend.content.homesection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "home_section_products")
@Getter
@Setter
@NoArgsConstructor
public class HomeSectionProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public HomeSectionProduct(UUID sectionId, UUID productId, int sortOrder) {
        this.sectionId = sectionId;
        this.productId = productId;
        this.sortOrder = sortOrder;
    }
}