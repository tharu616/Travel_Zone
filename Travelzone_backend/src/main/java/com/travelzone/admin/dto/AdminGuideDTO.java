package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminGuideDTO {
    private Long id;
    private String name;
    private String email;
    private String location;
    private String languages;
    private Double pricePerDay;
    private String status;
    private Double rating;
    private Integer totalBookings;
    private String profilePicture;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminGuideDTO obj = new AdminGuideDTO();
        public Builder id(Long v)             { obj.id = v; return this; }
        public Builder name(String v)         { obj.name = v; return this; }
        public Builder email(String v)        { obj.email = v; return this; }
        public Builder location(String v)     { obj.location = v; return this; }
        public Builder languages(String v)    { obj.languages = v; return this; }
        public Builder pricePerDay(Double v)  { obj.pricePerDay = v; return this; }
        public Builder status(String v)       { obj.status = v; return this; }
        public Builder rating(Double v)       { obj.rating = v; return this; }
        public Builder totalBookings(Integer v){ obj.totalBookings = v; return this; }
        public Builder profilePicture(String v){ obj.profilePicture = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminGuideDTO build()          { return obj; }
    }

    public Long getId()             { return id; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getLocation()     { return location; }
    public String getLanguages()    { return languages; }
    public Double getPricePerDay()  { return pricePerDay; }
    public String getStatus()       { return status; }
    public Double getRating()       { return rating; }
    public Integer getTotalBookings(){ return totalBookings; }
    public String getProfilePicture(){ return profilePicture; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
