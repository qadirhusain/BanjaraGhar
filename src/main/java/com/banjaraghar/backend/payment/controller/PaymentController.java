package com.banjaraghar.backend.payment.controller;

import com.banjaraghar.backend.order.dto.response.OrderResponse;
import com.banjaraghar.backend.payment.dto.request.VerifyPaymentRequest;
import com.banjaraghar.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public OrderResponse verify(@AuthenticationPrincipal UUID userId,
                                @Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verify(userId, request);
    }

    // Public in SecurityConfig: Razorpay cannot send a login token. The signature check is the protection.
    // The body is read as raw bytes because the signature is calculated over the exact bytes Razorpay sent.
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody byte[] rawBody,
                                        @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}