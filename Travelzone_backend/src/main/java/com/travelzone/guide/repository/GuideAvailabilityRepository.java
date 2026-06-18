package com.travelzone.guide.repository;

import com.travelzone.guide.entity.GuideAvailability;
import com.travelzone.guide.entity.GuideProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface GuideAvailabilityRepository extends JpaRepository<GuideAvailability, Long> {
    Optional<GuideAvailability> findByGuideProfileAndAvailableDate(GuideProfile guideProfile, LocalDate availableDate);
}
