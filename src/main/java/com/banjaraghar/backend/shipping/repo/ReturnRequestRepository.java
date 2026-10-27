package com.banjaraghar.backend.shipping.repo;

import com.banjaraghar.backend.shipping.entity.ReturnRequest;
import com.banjaraghar.backend.shipping.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {

    Optional<ReturnRequest> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    @Query("select r.order.id from ReturnRequest r where r.id = :id")
    Optional<UUID> findOrderIdById(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = {"order", "user"})
    Page<ReturnRequest> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"order", "user"})
    Page<ReturnRequest> findByStatus(ReturnStatus status, Pageable pageable);
}
