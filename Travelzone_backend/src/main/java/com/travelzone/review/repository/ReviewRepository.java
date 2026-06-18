package com.travelzone.review.repository;

import com.travelzone.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByGuideBookingIdInOrderByCreatedAtDesc(List<Long> guideBookingIds);

    List<Review> findByReservationIdInOrderByCreatedAtDesc(List<Long> reservationIds);

    Optional<Review> findByTouristIdAndGuideBookingId(Long touristId, Long guideBookingId);

    Optional<Review> findByTouristIdAndReservationId(Long touristId, Long reservationId);

    List<Review> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.guideBookingId IN :bookingIds")
    Double averageRatingForGuide(@Param("bookingIds") List<Long> bookingIds);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reservationId IN :reservationIds")
    Double averageRatingForHotel(@Param("reservationIds") List<Long> reservationIds);
}