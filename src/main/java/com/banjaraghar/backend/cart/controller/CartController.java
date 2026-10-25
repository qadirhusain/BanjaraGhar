package com.banjaraghar.backend.cart.controller;

import com.banjaraghar.backend.cart.dto.request.AddToCartRequest;
import com.banjaraghar.backend.cart.dto.request.UpdateCartItemRequest;
import com.banjaraghar.backend.cart.dto.response.CartResponse;
import com.banjaraghar.backend.cart.service.CartService;
import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UUID userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal UUID userId,
                                @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request);
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(@AuthenticationPrincipal UUID userId, @PathVariable UUID productId,
                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(userId, productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal UUID userId, @PathVariable UUID productId) {
        return cartService.removeItem(userId, productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal UUID userId) {
        cartService.clear(userId);
    }

    @GetMapping("/suggestions")
    public List<ProductSummaryResponse> suggestions(@AuthenticationPrincipal UUID userId) {
        return cartService.getSuggestions(userId);
    }
}