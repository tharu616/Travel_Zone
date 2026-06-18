package com.travelzone.guide.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "guide_availability")
public class GuideAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guide_profile_id", nullable = false)
    private GuideProfile guideProfile;

    @Column(nullable = false)
    private LocalDate availableDate;

    @Column(nullable = false)
    private boolean available = true;

    public GuideAvailability() {
    }

    public Long getId() {
        return id;
    }

    public GuideProfile getGuideProfile() {
        return guideProfile;
    }

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setGuideProfile(GuideProfile guideProfile) {
        this.guideProfile = guideProfile;
    }

    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
