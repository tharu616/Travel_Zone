package com.travelzone.guide.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateGuideProfileRequest {

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceYears;

    @NotEmpty(message = "At least one language is required")
    private List<String> languages;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal pricePerDay;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotBlank(message = "Profile photo is required")
    private String profilePhoto;

    public CreateGuideProfileRequest() {
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

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
