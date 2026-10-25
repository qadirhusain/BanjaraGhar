package com.banjaraghar.backend.wishlist.repo;

import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    @Query("""
            select p from WishlistItem w
            join w.product p
            join p.category c
            where w.user.id = :userId
              and p.active = true
              and c.active = true
            order by w.createdAt desc
            """)
    List<Product> findVisibleProducts(@Param("userId") UUID userId);

    @Modifying
    @Query(value = """
            insert into wishlist_items (user_id, product_id)
            values (:userId, :productId)
            on conflict (user_id, product_id) do nothing
            """, nativeQuery = true)
    void addIfAbsent(@Param("userId") UUID userId, @Param("productId") UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserId(UUID userId);
}
