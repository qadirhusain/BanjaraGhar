package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Every minute: orders still unpaid after their 30 minutes give their stock and coupon use back. */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnpaidOrderReleaseJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 60_000)
    public void releaseExpiredOrders() {
        List<UUID> ids = orderRepository.findIdsByStatusAndExpiresBefore(OrderStatus.PENDING_PAYMENT, Instant.now());

        int released = 0;
        for (UUID id : ids) {
            try {
                // Each order has its own transaction, so one failure cannot block the others
                if (orderService.releaseUnpaid(id, true)) {
                    released++;
                }
            } catch (Exception e) {
                log.error("Could not release unpaid order {}", id, e);
            }
        }
        if (released > 0) {
            log.info("Released {} unpaid orders", released);
        }
    }
}