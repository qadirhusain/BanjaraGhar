package com.banjaraghar.backend.order.repo;

import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.razorpayOrderId = :razorpayOrderId")
    Optional<Order> findByRazorpayOrderIdForUpdate(@Param("razorpayOrderId") String razorpayOrderId);

    @Query("select o.id from Order o where o.status = :status and o.expiresAt < :now")
    List<UUID> findIdsByStatusAndExpiresBefore(@Param("status") OrderStatus status, @Param("now") Instant now);

    @Query(value = "select nextval('order_number_seq')", nativeQuery = true)
    long nextOrderNumber();
}
