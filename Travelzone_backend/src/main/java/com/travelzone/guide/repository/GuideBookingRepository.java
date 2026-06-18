package com.travelzone.guide.repository;

import com.travelzone.guide.entity.GuideBooking;
import com.travelzone.guide.entity.GuideProfile;
import com.travelzone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuideBookingRepository extends JpaRepository<GuideBooking, Long> {

    // Tourist's bookings
    List<GuideBooking> findByTouristOrderByCreatedAtDesc(User tourist);

    // Guide's incoming requests
    List<GuideBooking> findByGuideProfileOrderByCreatedAtDesc(GuideProfile guideProfile);

    // ✅ Used by PaymentService and ReviewService — traverse guideProfile → user → id
    List<GuideBooking> findByGuideProfileUserIdOrderByCreatedAtDesc(Long userId);

    List<GuideBooking> findByTouristIdOrderByCreatedAtDesc(Long touristId);
}