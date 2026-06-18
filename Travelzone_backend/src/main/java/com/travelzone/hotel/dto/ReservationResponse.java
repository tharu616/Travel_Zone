package com.travelzone.hotel.dto;

import com.travelzone.common.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long reservationId;
    private Long hotelId;
    private String hotelName;       // ✅ added
    private String hotelLocation;   // ✅ added
    private Long roomId;
    private String roomType;        // ✅ added
    private Long touristId;
    private String touristName;     // ✅ added for hotel owner view
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt; // ✅ added

    public ReservationResponse(Long reservationId, Long hotelId, String hotelName, String hotelLocation,
                               Long roomId, String roomType, Long touristId, String touristName,
                               LocalDate checkIn, LocalDate checkOut,
                               BigDecimal totalPrice, BookingStatus status, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
        this.roomId = roomId;
        this.roomType = roomType;
        this.touristId = touristId;
        this.touristName = touristName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getReservationId() { return reservationId; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public String getHotelLocation() { return hotelLocation; }
    public Long getRoomId() { return roomId; }
    public String getRoomType() { return roomType; }
    public Long getTouristId() { return touristId; }
    public String getTouristName() { return touristName; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
