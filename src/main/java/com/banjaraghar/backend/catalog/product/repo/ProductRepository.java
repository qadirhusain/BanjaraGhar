package com.banjaraghar.backend.catalog.product.repo;

import com.banjaraghar.backend.catalog.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    // Admin list: loads the category together with the page, avoiding one query per row
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

    Optional<Product> findBySlugAndActiveTrueAndCategoryActiveTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);

    List<Product> findByIdIn(Collection<UUID> ids);

    List<Product> findByActiveTrueAndCategoryActiveTrue(Pageable pageable);

    List<Product> findByActiveTrueAndFeaturedTrueAndCategoryActiveTrue(Pageable pageable);

    List<Product> findByCategoryIdAndActiveTrueAndIdNotIn(UUID categoryId, Collection<UUID> excludedIds,
                                                          Pageable pageable);

    /** Ids of products matching a PostgreSQL text-search query (already sanitized by the service). */
    @Query(value = "select id from products where search_vector @@ to_tsquery('simple', :query)",
            nativeQuery = true)
    List<UUID> searchIds(@Param("query") String query);

    Optional<Product> findByIdAndActiveTrueAndCategoryActiveTrue(UUID id);

    @Query("""
            select p from Product p
            join p.category c
            where p.active = true
              and c.active = true
              and p.stockQuantity > 0
              and p.id not in :excludedIds
              and (p.featured = true or c.id in :categoryIds)
            order by p.createdAt desc
            """)
    List<Product> findSuggestions(@Param("excludedIds") Collection<UUID> excludedIds,
                                  @Param("categoryIds") Collection<UUID> categoryIds,
                                  Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query("update Product p set p.stockQuantity = p.stockQuantity - :quantity "
            + "where p.id = :id and p.stockQuantity >= :quantity")
    int reserveStock(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true)
    @Query("update Product p set p.stockQuantity = p.stockQuantity + :quantity where p.id = :id")
    void releaseStock(@Param("id") UUID id, @Param("quantity") int quantity);

    /** Visible products (with their category) for the given ids, in no particular order. */
    @Query("""
            select p from Product p join fetch p.category c
            where p.id in :ids and p.active = true and c.active = true
            """)
    List<Product> findVisibleByIdIn(@Param("ids") Collection<UUID> ids);
}