package com.banjaraghar.backend.catalog.category.repo;

import com.banjaraghar.backend.catalog.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByActiveTrueOrderBySortOrderAscNameAsc();

    List<Category> findAllByOrderBySortOrderAscNameAsc();

    Optional<Category> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
