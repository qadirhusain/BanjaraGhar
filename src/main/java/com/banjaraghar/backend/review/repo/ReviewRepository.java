package com.banjaraghar.backend.review.repo;

import com.banjaraghar.backend.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Load the reviewer together with the page, avoiding one query per row
    @EntityGraph(attributePaths = "user")
    Page<Review> findByProductIdAndVisibleTrue(UUID productId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<Review> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Override
    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findByVisible(boolean visible, Pageable pageable);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    @EntityGraph(attributePaths = "user")
    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select avg(r.rating) as average, count(r) as total
            from Review r
            where r.product.id = :productId and r.visible = true
            """)
    RatingSummary summarize(@Param("productId") UUID productId);

    /** Verified buyer check: the customer has received this product in a delivered order. */
    @Query("""
            select count(oi) > 0 from OrderItem oi
            where oi.order.user.id = :userId and oi.product.id = :productId
              and oi.order.status = com.banjaraghar.backend.order.enums.OrderStatus.DELIVERED
            """)
    boolean hasDeliveredOrder(@Param("userId") UUID userId, @Param("productId") UUID productId);
}