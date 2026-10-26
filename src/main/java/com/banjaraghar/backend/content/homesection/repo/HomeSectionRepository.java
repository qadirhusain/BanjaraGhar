package com.banjaraghar.backend.content.homesection.repo;

import com.banjaraghar.backend.content.homesection.entity.HomeSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HomeSectionRepository extends JpaRepository<HomeSection, UUID> {

    List<HomeSection> findByActiveTrueOrderBySortOrderAscCreatedAtAsc();

    List<HomeSection> findAllByOrderBySortOrderAscCreatedAtAsc();
}