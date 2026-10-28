package com.banjaraghar.backend.dashboard.service;

import com.banjaraghar.backend.dashboard.dto.response.DashboardResponse;
import com.banjaraghar.backend.dashboard.repo.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final ZoneId STORE_ZONE = ZoneId.of("Asia/Kolkata");

    // Same limit the storefront uses for "Only few left"
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final DashboardRepository dashboardRepository;

    @Transactional(readOnly = true)
    public DashboardResponse get() {
        Instant startOfToday = LocalDate.now(STORE_ZONE).atStartOfDay(STORE_ZONE).toInstant();
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        return new DashboardResponse(
                dashboardRepository.salesSince(startOfToday),
                dashboardRepository.ordersSince(startOfToday),
                dashboardRepository.salesSince(thirtyDaysAgo),
                dashboardRepository.ordersSince(thirtyDaysAgo),
                dashboardRepository.salesSince(Instant.EPOCH),
                dashboardRepository.ordersSince(Instant.EPOCH),
                dashboardRepository.ordersToShip(),
                dashboardRepository.shipmentsNeedingAttention(),
                dashboardRepository.returnsAwaitingReview(),
                dashboardRepository.newMessages(),
                dashboardRepository.lowStockProducts(LOW_STOCK_THRESHOLD),
                dashboardRepository.outOfStockProducts(),
                dashboardRepository.customers(),
                dashboardRepository.activeSubscribers());
    }
}