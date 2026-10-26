package com.banjaraghar.backend.order.repo;

import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdIn(Collection<UUID> orderIds);

    /** Product ids ranked by units sold, counting only orders that were really bought. */
    @Query("""
            select oi.product.id from OrderItem oi
            where oi.order.status in :statuses
            group by oi.product.id
            order by sum(oi.quantity) desc
            """)
    List<UUID> findBestSellerProductIds(@Param("statuses") Collection<OrderStatus> statuses, Pageable pageable);
}