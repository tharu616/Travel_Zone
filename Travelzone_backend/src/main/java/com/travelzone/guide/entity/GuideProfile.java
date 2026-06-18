package com.travelzone.guide.entity;

import com.travelzone.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_profiles")
public class GuideProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer experienceYears;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guide_languages", joinColumns = @JoinColumn(name = "guide_profile_id"))
    @Column(name = "language")
    private List<String> languages = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 1000)
    private String bio;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String profilePhoto;

    @Column(nullable = false)
    private Double rating = 0.0;

    public GuideProfile() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getExperienceYears() { return experienceYears; }
    public List<String> getLanguages() { return languages; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getProfilePhoto() { return profilePhoto; }
    public Double getRating() { return rating; }

    public void setUser(User user) { this.user = user; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setLocation(String location) { this.location = location; }
    public void setBio(String bio) { this.bio = bio; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public void setRating(Double rating) { this.rating = rating; }
}