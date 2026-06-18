package com.travelzone.hotel.repository;

import com.travelzone.hotel.entity.Hotel;
import com.travelzone.hotel.entity.Reservation;
import com.travelzone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Tourist's own reservations (by User object)
    List<Reservation> findByTouristOrderByCreatedAtDesc(User tourist);

    // Tourist's own reservations (by tourist ID)
    List<Reservation> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    // All reservations for a specific hotel
    List<Reservation> findByHotelOrderByCreatedAtDesc(Hotel hotel);

    // ✅ FIXED — JOIN FETCH prevents LazyInitializationException
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.hotel h " +
           "JOIN FETCH h.owner " +
           "JOIN FETCH r.room " +
           "JOIN FETCH r.tourist " +
           "WHERE h.owner = :owner " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findByHotelOwnerOrderByCreatedAtDesc(@Param("owner") User owner);
}