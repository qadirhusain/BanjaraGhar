package com.banjaraghar.backend.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminReviewResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String reviewerName;
    private String reviewerEmail;
    private int rating;
    private String comment;
    private boolean visible;
    private Instant createdAt;
}