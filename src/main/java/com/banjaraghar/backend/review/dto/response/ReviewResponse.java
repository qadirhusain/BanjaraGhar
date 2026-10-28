package com.banjaraghar.backend.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID productId;
    private int rating;
    private String comment;
    private String reviewerName;     // first name and last initial, e.g. "Qadir H."
    private Instant createdAt;
}