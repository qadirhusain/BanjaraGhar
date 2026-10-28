package com.banjaraghar.backend.activitylog.repo;

import com.banjaraghar.backend.activitylog.entity.ActivityLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    // Load the activitylog together with the page, avoiding one query per row
    @Override
    @EntityGraph(attributePaths = "activitylog")
    Page<ActivityLog> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "activitylog")
    Page<ActivityLog> findByEntityType(String entityType, Pageable pageable);
}
