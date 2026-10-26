package com.banjaraghar.backend.content.banner.entity;

import com.banjaraghar.backend.catalog.category.entity.Category;
import com.banjaraghar.backend.content.banner.enums.BannerPlacement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String subtitle;

    @Column(name = "button_text")
    private String buttonText;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "mobile_image_url")
    private String mobileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerPlacement placement;

    /** Only set for CATEGORY banners. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}