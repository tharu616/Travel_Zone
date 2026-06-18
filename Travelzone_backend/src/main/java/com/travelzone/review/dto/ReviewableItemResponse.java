package com.travelzone.review.dto;

import java.time.LocalDate;

public class ReviewableItemResponse {

    private Long id;
    private String type;          // "GUIDE_BOOKING" or "HOTEL_RESERVATION"
    private String name;          // guide name or hotel name
    private String location;      // guide location or hotel location
    private LocalDate startDate;  // first day of the booking/reservation
    private LocalDate endDate;    // last day (same as startDate for single-day)
    private boolean alreadyReviewed;

    // For guide bookings: expose the booking date via guideBookingId-compatible field
    private Long guideBookingId;      // primary booking ID (used when submitting review)
    private Long reservationId;       // for hotel reservations

    public ReviewableItemResponse(Long id, String type, String name, String location,
                                   LocalDate startDate, LocalDate endDate,
                                   boolean alreadyReviewed) {
        this.id              = id;
        this.type            = type;
        this.name            = name;
        this.location        = location;
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.alreadyReviewed = alreadyReviewed;

        if ("GUIDE_BOOKING".equals(type))      this.guideBookingId = id;
        else if ("HOTEL_RESERVATION".equals(type)) this.reservationId = id;
    }

    public Long getId()               { return id; }
    public String getType()           { return type; }
    public String getName()           { return name; }
    public String getLocation()       { return location; }
    public LocalDate getStartDate()   { return startDate; }
    public LocalDate getEndDate()     { return endDate; }
    public boolean isAlreadyReviewed(){ return alreadyReviewed; }
    public Long getGuideBookingId()   { return guideBookingId; }
    public Long getReservationId()    { return reservationId; }

    // Frontend compat aliases
    public String getGuideName()      { return "GUIDE_BOOKING".equals(type) ? name : null; }
    public String getHotelName()      { return "HOTEL_RESERVATION".equals(type) ? name : null; }
    public String getGuideLocation()  { return "GUIDE_BOOKING".equals(type) ? location : null; }
    public String getHotelLocation()  { return "HOTEL_RESERVATION".equals(type) ? location : null; }
    public LocalDate getBookingDate() { return startDate; }
    public LocalDate getCheckInDate() { return "HOTEL_RESERVATION".equals(type) ? startDate : null; }
    public LocalDate getCheckOutDate(){ return "HOTEL_RESERVATION".equals(type) ? endDate : null; }
}