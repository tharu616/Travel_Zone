package com.travelzone.guide.dto;

import com.travelzone.common.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GuideBookingResponse {

    private Long bookingId;
    private Long guideProfileId;
    private String guideName;
    private String guideProfilePhoto;
    private Long touristId;
    private String touristName;
    private LocalDate bookingDate;   // the specific day this row covers
    private LocalDate endDate;       // last day of the range (same as bookingDate for single-day)
    private BigDecimal totalPrice;   // price for this single day
    private BookingStatus status;
    private LocalDateTime createdAt;

    public GuideBookingResponse(Long bookingId, Long guideProfileId, String guideName,
                                 String guideProfilePhoto, Long touristId, String touristName,
                                 LocalDate bookingDate, LocalDate endDate,
                                 BigDecimal totalPrice, BookingStatus status,
                                 LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.guideProfileId = guideProfileId;
        this.guideName = guideName;
        this.guideProfilePhoto = guideProfilePhoto;
        this.touristId = touristId;
        this.touristName = touristName;
        this.bookingDate = bookingDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getBookingId() { return bookingId; }
    public Long getGuideProfileId() { return guideProfileId; }
    public String getGuideName() { return guideName; }
    public String getGuideProfilePhoto() { return guideProfilePhoto; }
    public Long getTouristId() { return touristId; }
    public String getTouristName() { return touristName; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}