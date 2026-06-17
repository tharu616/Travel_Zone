package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminHotelDTO {
    private Long id;
    private String name;
    private String location;
    private String ownerName;
    private String ownerEmail;
    private Integer totalRooms;
    private String status;
    private Double rating;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminHotelDTO obj = new AdminHotelDTO();
        public Builder id(Long v)           { obj.id = v; return this; }
        public Builder name(String v)       { obj.name = v; return this; }
        public Builder location(String v)   { obj.location = v; return this; }
        public Builder ownerName(String v)  { obj.ownerName = v; return this; }
        public Builder ownerEmail(String v) { obj.ownerEmail = v; return this; }
        public Builder totalRooms(Integer v){ obj.totalRooms = v; return this; }
        public Builder status(String v)     { obj.status = v; return this; }
        public Builder rating(Double v)     { obj.rating = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminHotelDTO build()        { return obj; }
    }

    public Long getId()           { return id; }
    public String getName()       { return name; }
    public String getLocation()   { return location; }
    public String getOwnerName()  { return ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public Integer getTotalRooms(){ return totalRooms; }
    public String getStatus()     { return status; }
    public Double getRating()     { return rating; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
