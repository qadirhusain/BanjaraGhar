package com.banjaraghar.backend.order.controller;

import com.banjaraghar.backend.order.dto.request.CheckoutRequest;
import com.banjaraghar.backend.order.dto.response.CheckoutResponse;
import com.banjaraghar.backend.order.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@AuthenticationPrincipal UUID userId,
                                     @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(userId, request);
    }
}