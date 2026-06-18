package com.travelzone.review.dto;

import java.time.LocalDate;

public class ReviewableReservationDto {

    private Long reservationId;
    private String hotelName;
    private String hotelLocation;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public ReviewableReservationDto(Long reservationId, String hotelName,
                                     String hotelLocation,
                                     LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationId = reservationId;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public Long getReservationId() { return reservationId; }
    public String getHotelName() { return hotelName; }
    public String getHotelLocation() { return hotelLocation; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
}