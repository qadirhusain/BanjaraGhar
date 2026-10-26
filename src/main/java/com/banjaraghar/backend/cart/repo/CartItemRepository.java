package com.banjaraghar.backend.cart.repo;

import com.banjaraghar.backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Loads product and category together with the cart, avoiding one query per row
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<CartItem> findByUserIdOrderByProductNameAsc(UUID userId);

    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    // Insert-or-update, so a double tap on "Add to cart" can never hit the unique constraint.
    // clearAutomatically makes the next read see the new quantity instead of a stale loaded entity.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            insert into cart_items (user_id, product_id, quantity)
            values (:userId, :productId, :quantity)
            on conflict (user_id, product_id)
            do update set quantity = :quantity, updated_at = now()
            """, nativeQuery = true)
    void upsertQuantity(@Param("userId") UUID userId, @Param("productId") UUID productId,
                        @Param("quantity") int quantity);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserId(UUID userId);

    void deleteByUserIdAndProductIdIn(UUID userId, Collection<UUID> productIds);
}