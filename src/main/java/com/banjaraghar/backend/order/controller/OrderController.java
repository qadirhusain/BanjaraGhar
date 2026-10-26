package com.banjaraghar.backend.order.controller;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.order.dto.response.OrderResponse;
import com.banjaraghar.backend.order.dto.response.OrderSummaryResponse;
import com.banjaraghar.backend.order.service.OrderCancellationService;
import com.banjaraghar.backend.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderCancellationService cancellationService;

    @GetMapping
    public PageResponse<OrderSummaryResponse> list(@AuthenticationPrincipal UUID userId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return orderService.listOrders(userId, page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return orderService.getOrder(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return cancellationService.cancelByCustomer(userId, id);
    }
}