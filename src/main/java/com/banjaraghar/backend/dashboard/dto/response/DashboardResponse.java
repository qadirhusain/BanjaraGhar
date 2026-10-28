package com.banjaraghar.backend.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardResponse {

    // Sales are in paise: paid, shipped and delivered orders, minus refunds
    private long salesToday;
    private long ordersToday;
    private long salesLast30Days;
    private long ordersLast30Days;
    private long totalSales;
    private long totalOrders;

    // Things that need the admin's attention
    private long ordersToShip;
    private long shipmentsNeedingAttention;
    private long returnsAwaitingReview;
    private long newMessages;
    private long lowStockProducts;
    private long outOfStockProducts;

    // Audience
    private long totalCustomers;
    private long newsletterSubscribers;
}