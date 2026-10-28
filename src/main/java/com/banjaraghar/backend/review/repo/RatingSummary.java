package com.banjaraghar.backend.review.repo;

public interface RatingSummary {

    Double getAverage();   // null when there are no reviews

    Long getTotal();
}