package com.banjaraghar.backend.content.homesection.repo;

import com.banjaraghar.backend.content.homesection.entity.HomeSectionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HomeSectionProductRepository extends JpaRepository<HomeSectionProduct, UUID> {

    List<HomeSectionProduct> findBySectionIdInOrderBySortOrderAsc(Collection<UUID> sectionIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from HomeSectionProduct sp where sp.sectionId = :sectionId")
    void deleteBySectionId(@Param("sectionId") UUID sectionId);
}