package com.banjaraghar.backend.review.controller;

import com.banjaraghar.backend.review.dto.request.CreateReviewRequest;
import com.banjaraghar.backend.review.dto.request.ReviewRequest;
import com.banjaraghar.backend.review.dto.response.ReviewListResponse;
import com.banjaraghar.backend.review.dto.response.ReviewResponse;
import com.banjaraghar.backend.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Public: the product page
    @GetMapping("/products/{slug}/reviews")
    public ReviewListResponse listForProduct(@PathVariable String slug,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return reviewService.listForProduct(slug, page, size);
    }

    // Signed-in customer
    @GetMapping("/reviews/mine")
    public List<ReviewResponse> mine(@AuthenticationPrincipal UUID userId) {
        return reviewService.listMine(userId);
    }

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal UUID userId,
                                 @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(userId, request);
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse update(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.update(userId, id, request);
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        reviewService.delete(userId, id);
    }
}