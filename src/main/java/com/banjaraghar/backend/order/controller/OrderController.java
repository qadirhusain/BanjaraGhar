package com.banjaraghar.backend.order.controller;

import com.banjaraghar.backend.order.dto.response.OrderResponse;
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

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return orderService.getOrder(userId, id);
    }
}