package com.travelzone.guide.dto;

import java.math.BigDecimal;

public class GuideSearchResponse {

    private Long guideId;
    private String name;
    private String profilePhoto;
    private Double rating;
    private BigDecimal pricePerDay;

    public GuideSearchResponse(Long guideId, String name, String profilePhoto, Double rating, BigDecimal pricePerDay) {
        this.guideId = guideId;
        this.name = name;
        this.profilePhoto = profilePhoto;
        this.rating = rating;
        this.pricePerDay = pricePerDay;
    }

    public Long getGuideId() {
        return guideId;
    }

    public String getName() {
        return name;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public Double getRating() {
        return rating;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }
}
