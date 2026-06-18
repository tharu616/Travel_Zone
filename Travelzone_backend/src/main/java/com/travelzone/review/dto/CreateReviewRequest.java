package com.travelzone.review.dto;

import jakarta.validation.constraints.*;

public class CreateReviewRequest {

    private Long guideBookingId;   // provide one
    private Long reservationId;    // or the other

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must be at most 1000 characters")
    private String comment;

    public CreateReviewRequest() {}
    public Long getGuideBookingId() { return guideBookingId; }
    public void setGuideBookingId(Long guideBookingId) { this.guideBookingId = guideBookingId; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}