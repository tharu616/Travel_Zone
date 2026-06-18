package com.travelzone.review.dto;

import java.time.LocalDate;

public class ReviewableGuideBookingDto {
    private Long guideBookingId;
    private String guideName;
    private String guideLocation;
    private LocalDate bookingDate;

    public ReviewableGuideBookingDto(Long guideBookingId, String guideName,
                                     String guideLocation, LocalDate bookingDate) {
        this.guideBookingId = guideBookingId;
        this.guideName = guideName;
        this.guideLocation = guideLocation;
        this.bookingDate = bookingDate;
    }

    public Long getGuideBookingId() { return guideBookingId; }
    public String getGuideName() { return guideName; }
    public String getGuideLocation() { return guideLocation; }
    public LocalDate getBookingDate() { return bookingDate; }
}