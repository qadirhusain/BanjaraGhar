package com.banjaraghar.backend.content.banner.repo;

import com.banjaraghar.backend.content.banner.entity.Banner;
import com.banjaraghar.backend.content.banner.enums.BannerPlacement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {

    /** Home banners that are switched on and inside their dates. */
    @Query("""
            select b from Banner b
            where b.placement = :placement and b.active = true
              and (b.startsAt is null or b.startsAt <= :now)
              and (b.endsAt is null or b.endsAt > :now)
            order by b.sortOrder asc, b.createdAt desc
            """)
    List<Banner> findLive(@Param("placement") BannerPlacement placement, @Param("now") Instant now);

    /** The same for one category's page. */
    @Query("""
            select b from Banner b
            where b.placement = com.banjaraghar.backend.content.banner.enums.BannerPlacement.CATEGORY
              and b.category.id = :categoryId and b.active = true
              and (b.startsAt is null or b.startsAt <= :now)
              and (b.endsAt is null or b.endsAt > :now)
            order by b.sortOrder asc, b.createdAt desc
            """)
    List<Banner> findLiveForCategory(@Param("categoryId") UUID categoryId, @Param("now") Instant now);

    // Admin lists: load the category together with the banners, avoiding one query per row
    @EntityGraph(attributePaths = "category")
    List<Banner> findAllByOrderByPlacementAscSortOrderAscCreatedAtDesc();

    @EntityGraph(attributePaths = "category")
    List<Banner> findByPlacementOrderBySortOrderAscCreatedAtDesc(BannerPlacement placement);
}