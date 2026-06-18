package com.travelzone.guide.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GuideProfileResponse {

    private Long guideId;
    private String name;
    private Integer experienceYears;
    private List<String> languages;
    private BigDecimal pricePerDay;
    private String location;
    private String bio;
    private String profilePhoto;
    private Double rating;
    private List<LocalDate> availableDates;

    public GuideProfileResponse(Long guideId, String name, Integer experienceYears, List<String> languages,
                                BigDecimal pricePerDay, String location, String bio, String profilePhoto,
                                Double rating, List<LocalDate> availableDates) {
        this.guideId = guideId;
        this.name = name;
        this.experienceYears = experienceYears;
        this.languages = languages;
        this.pricePerDay = pricePerDay;
        this.location = location;
        this.bio = bio;
        this.profilePhoto = profilePhoto;
        this.rating = rating;
        this.availableDates = availableDates;
    }

    public Long getGuideId() {
        return guideId;
    }

    public String getName() {
        return name;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public Double getRating() {
        return rating;
    }

    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }
}
