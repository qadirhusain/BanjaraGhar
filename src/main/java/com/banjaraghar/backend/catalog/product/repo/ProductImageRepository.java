package com.banjaraghar.backend.catalog.product.repo;

import com.banjaraghar.backend.catalog.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(UUID productId);

    List<ProductImage> findByProductIdInOrderBySortOrderAsc(Collection<UUID> productIds);

    Optional<ProductImage> findByIdAndProductId(UUID id, UUID productId);

    long countByProductId(UUID productId);
}