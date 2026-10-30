package com.banjaraghar.backend.wishlist.controller;

import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class
WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public List<ProductSummaryResponse> list(@AuthenticationPrincipal UUID userId) {
        return wishlistService.list(userId);
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductSummaryResponse add(@AuthenticationPrincipal UUID userId, @PathVariable UUID productId) {
        return wishlistService.add(userId, productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal UUID userId, @PathVariable UUID productId) {
        wishlistService.remove(userId, productId);
    }
}