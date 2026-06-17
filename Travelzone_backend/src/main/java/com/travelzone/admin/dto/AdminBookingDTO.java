package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminBookingDTO {
    private Long id;
    private String touristName;
    private String touristEmail;
    private String guideName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Double totalPrice;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminBookingDTO obj = new AdminBookingDTO();
        public Builder id(Long v)              { obj.id = v; return this; }
        public Builder touristName(String v)   { obj.touristName = v; return this; }
        public Builder touristEmail(String v)  { obj.touristEmail = v; return this; }
        public Builder guideName(String v)     { obj.guideName = v; return this; }
        public Builder startDate(LocalDateTime v){ obj.startDate = v; return this; }
        public Builder endDate(LocalDateTime v){ obj.endDate = v; return this; }
        public Builder status(String v)        { obj.status = v; return this; }
        public Builder totalPrice(Double v)    { obj.totalPrice = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminBookingDTO build()         { return obj; }
    }

    public Long getId()              { return id; }
    public String getTouristName()   { return touristName; }
    public String getTouristEmail()  { return touristEmail; }
    public String getGuideName()     { return guideName; }
    public LocalDateTime getStartDate(){ return startDate; }
    public LocalDateTime getEndDate(){ return endDate; }
    public String getStatus()        { return status; }
    public Double getTotalPrice()    { return totalPrice; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
