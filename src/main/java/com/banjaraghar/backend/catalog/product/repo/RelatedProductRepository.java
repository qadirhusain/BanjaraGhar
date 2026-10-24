package com.banjaraghar.backend.catalog.product.repo;

import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.entity.RelatedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RelatedProductRepository extends JpaRepository<RelatedProduct, UUID> {

    @Query("""
            select rp.relatedProduct from RelatedProduct rp
            where rp.product.id = :productId
              and rp.relatedProduct.active = true
              and rp.relatedProduct.category.active = true
            order by rp.sortOrder
            """)
    List<Product> findVisibleRelated(@Param("productId") UUID productId);

    @Query("select rp.relatedProduct.id from RelatedProduct rp where rp.product.id = :productId order by rp.sortOrder")
    List<UUID> findRelatedIds(@Param("productId") UUID productId);

    @Modifying(flushAutomatically = true)
    @Query("delete from RelatedProduct rp where rp.product.id = :productId")
    void clearForProduct(@Param("productId") UUID productId);
}
