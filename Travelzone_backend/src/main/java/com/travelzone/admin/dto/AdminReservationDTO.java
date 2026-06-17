package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminReservationDTO {
    private Long id;
    private String guestName;
    private String guestEmail;
    private String hotelName;
    private String roomType;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private Double totalPrice;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminReservationDTO obj = new AdminReservationDTO();
        public Builder id(Long v)           { obj.id = v; return this; }
        public Builder guestName(String v)  { obj.guestName = v; return this; }
        public Builder guestEmail(String v) { obj.guestEmail = v; return this; }
        public Builder hotelName(String v)  { obj.hotelName = v; return this; }
        public Builder roomType(String v)   { obj.roomType = v; return this; }
        public Builder checkIn(LocalDateTime v){ obj.checkIn = v; return this; }
        public Builder checkOut(LocalDateTime v){ obj.checkOut = v; return this; }
        public Builder status(String v)     { obj.status = v; return this; }
        public Builder totalPrice(Double v) { obj.totalPrice = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminReservationDTO build()  { return obj; }
    }

    public Long getId()           { return id; }
    public String getGuestName()  { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public String getHotelName()  { return hotelName; }
    public String getRoomType()   { return roomType; }
    public LocalDateTime getCheckIn()  { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public String getStatus()     { return status; }
    public Double getTotalPrice() { return totalPrice; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
