package com.banjaraghar.backend.order.controller;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.order.dto.response.AdminOrderResponse;
import com.banjaraghar.backend.order.dto.response.AdminOrderSummaryResponse;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public PageResponse<AdminOrderSummaryResponse> list(@RequestParam(required = false) OrderStatus status,
                                                        @RequestParam(required = false) String q,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return adminOrderService.list(status, q, page, size);
    }

    @GetMapping("/{id}")
    public AdminOrderResponse get(@PathVariable UUID id) {
        return adminOrderService.get(id);
    }

    @PostMapping("/{id}/cancel")
    public AdminOrderResponse cancel(@PathVariable UUID id) {
        return adminOrderService.cancel(id);
    }
}