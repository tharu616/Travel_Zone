package com.travelzone.guide.repository;

import com.travelzone.guide.entity.GuideProfile;
import com.travelzone.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuideProfileRepository extends JpaRepository<GuideProfile, Long> {

    boolean existsByUser(User user);

    // Used by /me endpoints
    Optional<GuideProfile> findByUser(User user);

    Page<GuideProfile> findByLocationContainingIgnoreCaseAndLanguagesContainingAndRatingGreaterThanEqual(
            String location, String language, Double rating, Pageable pageable);

    Page<GuideProfile> findByLocationContainingIgnoreCaseAndRatingGreaterThanEqual(
            String location, Double rating, Pageable pageable);

    // ✅ NEW — required by ReviewService to recalculate guide rating after reviews
    Optional<GuideProfile> findByUserId(Long userId);
}