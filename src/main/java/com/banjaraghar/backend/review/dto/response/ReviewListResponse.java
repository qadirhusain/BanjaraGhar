package com.banjaraghar.backend.review.dto.response;

import com.banjaraghar.backend.common.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewListResponse {

    private double averageRating;     // one decimal, 0 when there are no reviews
    private long reviewCount;
    private PageResponse<ReviewResponse> reviews;
}