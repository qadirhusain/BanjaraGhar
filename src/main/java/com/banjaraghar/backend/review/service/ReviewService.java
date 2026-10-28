package com.banjaraghar.backend.review.service;

import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.common.util.TextUtil;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ForbiddenException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.review.dto.request.CreateReviewRequest;
import com.banjaraghar.backend.review.dto.request.ReviewRequest;
import com.banjaraghar.backend.review.dto.response.AdminReviewResponse;
import com.banjaraghar.backend.review.dto.response.ReviewListResponse;
import com.banjaraghar.backend.review.dto.response.ReviewResponse;
import com.banjaraghar.backend.review.entity.Review;
import com.banjaraghar.backend.review.repo.RatingSummary;
import com.banjaraghar.backend.review.repo.ReviewRepository;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    // ------------------------------------------------------------------ public

    @Transactional(readOnly = true)
    public ReviewListResponse listForProduct(String slug, int page, int size) {
        Product product = productRepository.findBySlugAndActiveTrueAndCategoryActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Page<Review> reviews = reviewRepository.findByProductIdAndVisibleTrue(product.getId(), newestFirst(page, size));
        RatingSummary summary = reviewRepository.summarize(product.getId());

        double average = summary.getAverage() == null ? 0 : Math.round(summary.getAverage() * 10) / 10.0;
        long count = summary.getTotal() == null ? 0 : summary.getTotal();

        return new ReviewListResponse(average, count,
                PageResponse.of(reviews, reviews.getContent().stream().map(this::toResponse).toList()));
    }

    // ---------------------------------------------------------------- customer

    @Transactional(readOnly = true)
    public List<ReviewResponse> listMine(UUID userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse create(UUID userId, CreateReviewRequest request) {
        User user = userService.getActiveUser(userId);
        Product product = productRepository.findById(request.getProductId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByUserIdAndProductId(userId, product.getId())) {
            throw new ConflictException("You have already reviewed this product");
        }
        if (!reviewRepository.hasDeliveredOrder(userId, product.getId())) {
            throw new ForbiddenException("Only customers who have received this product can review it");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(TextUtil.blankToNull(request.getComment()));

        try {
            return toResponse(reviewRepository.saveAndFlush(review));
        } catch (DataIntegrityViolationException e) {
            // Two requests arrived at once; the unique constraint caught the second
            throw new ConflictException("You have already reviewed this product");
        }
    }

    @Transactional
    public ReviewResponse update(UUID userId, UUID reviewId, ReviewRequest request) {
        Review review = findOwn(userId, reviewId);
        review.setRating(request.getRating());
        review.setComment(TextUtil.blankToNull(request.getComment()));
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(UUID userId, UUID reviewId) {
        reviewRepository.delete(findOwn(userId, reviewId));
    }

    // ------------------------------------------------------------------- activitylog

    @Transactional(readOnly = true)
    public PageResponse<AdminReviewResponse> listForAdmin(Boolean visible, int page, int size) {
        Pageable pageable = newestFirst(page, size);
        Page<Review> reviews = visible == null
                ? reviewRepository.findAll(pageable)
                : reviewRepository.findByVisible(visible, pageable);
        return PageResponse.of(reviews, reviews.getContent().stream().map(this::toAdminResponse).toList());
    }

    @Transactional
    public AdminReviewResponse setVisible(UUID reviewId, boolean visible) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setVisible(visible);
        return toAdminResponse(reviewRepository.save(review));
    }

    // ----------------------------------------------------------------- helpers

    private Review findOwn(UUID userId, UUID reviewId) {
        return reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private Pageable newestFirst(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getProduct().getId(), review.getRating(),
                review.getComment(), displayName(review.getUser()), review.getCreatedAt());
    }

    private AdminReviewResponse toAdminResponse(Review review) {
        User user = review.getUser();
        return new AdminReviewResponse(review.getId(), review.getProduct().getId(), review.getProduct().getName(),
                user.getId(), user.getFullName(), user.getEmail(), review.getRating(), review.getComment(),
                review.isVisible(), review.getCreatedAt());
    }

    /** Public name: first name plus last initial. Deleted accounts show a neutral label. */
    private String displayName(User user) {
        if (user.getDeletedAt() != null) {
            return "Former customer";
        }
        String[] parts = user.getFullName().trim().split("\\s+");
        return parts.length > 1 ? parts[0] + " " + parts[parts.length - 1].charAt(0) + "." : parts[0];
    }
}
