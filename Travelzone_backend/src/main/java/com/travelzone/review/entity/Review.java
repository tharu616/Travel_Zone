package com.travelzone.review.entity;

import com.travelzone.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tourist_id", "guide_booking_id"}),
        @UniqueConstraint(columnNames = {"tourist_id", "reservation_id"})
    }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tourist_id", nullable = false)
    private User tourist;

    @Column(name = "guide_booking_id")
    private Long guideBookingId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(nullable = false)
    private int rating; // 1–5

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Review() {}

    public Long getId() { return id; }
    public User getTourist() { return tourist; }
    public void setTourist(User tourist) { this.tourist = tourist; }
    public Long getGuideBookingId() { return guideBookingId; }
    public void setGuideBookingId(Long guideBookingId) { this.guideBookingId = guideBookingId; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}