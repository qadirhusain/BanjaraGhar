package com.banjaraghar.backend.review.controller;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.review.dto.request.VisibleRequest;
import com.banjaraghar.backend.review.dto.response.AdminReviewResponse;
import com.banjaraghar.backend.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public PageResponse<AdminReviewResponse> list(@RequestParam(required = false) Boolean visible,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return reviewService.listForAdmin(visible, page, size);
    }

    @PatchMapping("/{id}/visible")
    public AdminReviewResponse setVisible(@PathVariable UUID id, @Valid @RequestBody VisibleRequest request) {
        return reviewService.setVisible(id, request.getVisible());
    }
}