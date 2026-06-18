package com.travelzone.review.dto;

import java.time.LocalDateTime;

public class ReviewResponse {
    private Long id;
    private Long touristId;
    private String touristName;
    private Long guideBookingId;
    private Long reservationId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewResponse(Long id, Long touristId, String touristName,
                          Long guideBookingId, Long reservationId,
                          int rating, String comment,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.touristId = touristId; this.touristName = touristName;
        this.guideBookingId = guideBookingId; this.reservationId = reservationId;
        this.rating = rating; this.comment = comment;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getTouristId() { return touristId; }
    public String getTouristName() { return touristName; }
    public Long getGuideBookingId() { return guideBookingId; }
    public Long getReservationId() { return reservationId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}