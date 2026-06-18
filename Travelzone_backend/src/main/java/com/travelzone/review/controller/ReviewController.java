package com.travelzone.review.controller;

import com.travelzone.common.dto.ApiResponse;
import com.travelzone.review.dto.*;
import com.travelzone.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(request, authentication.getName()));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                reviewService.updateReview(reviewId, request, authentication.getName()));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        reviewService.deleteReview(reviewId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse(true, "Review deleted successfully"));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getMyReviews(authentication.getName()));
    }

    @GetMapping("/guide-reviews")
    public ResponseEntity<List<ReviewResponse>> getGuideReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getGuideReviews(authentication.getName()));
    }

    @GetMapping("/hotel-reviews")
    public ResponseEntity<List<ReviewResponse>> getHotelReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getHotelOwnerReviews(authentication.getName()));
    }

    // ── Returns grouped guide bookings (1 per trip, not 1 per day) ───────────
    @GetMapping("/reviewable-guide-bookings")
    public ResponseEntity<List<ReviewableItemResponse>> getReviewableGuideBookings(
            Authentication authentication) {
        return ResponseEntity.ok(
                reviewService.getReviewableGuideBookings(authentication.getName()));
    }

    // ── Returns completed hotel reservations ─────────────────────────────────
    @GetMapping("/reviewable-reservations")
    public ResponseEntity<List<ReviewableItemResponse>> getReviewableReservations(
            Authentication authentication) {
        return ResponseEntity.ok(
                reviewService.getReviewableReservations(authentication.getName()));
    }
}