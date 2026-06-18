package com.travelzone.hotel.repository;

import com.travelzone.hotel.entity.Hotel;
import com.travelzone.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Original — location + price only
    Page<Hotel> findByActiveTrueAndLocationContainingIgnoreCaseAndMinPriceLessThanEqual(
            String location, BigDecimal price, Pageable pageable);

    // ✅ NEW — location + price + facility filter
    @Query("""
        SELECT DISTINCT h FROM Hotel h
        JOIN h.facilities f
        WHERE h.active = true
        AND UPPER(h.location) LIKE UPPER(CONCAT('%', :location, '%'))
        AND h.minPrice <= :price
        AND UPPER(f) LIKE UPPER(CONCAT('%', :facility, '%'))
        ORDER BY h.rating DESC
        """)
    Page<Hotel> findByActiveTrueAndLocationAndPriceAndFacility(
            @Param("location") String location,
            @Param("price")    BigDecimal price,
            @Param("facility") String facility,
            Pageable pageable);

    List<Hotel> findByOwnerOrderByIdDesc(User owner);

    @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.owner ORDER BY h.id DESC")
    List<Hotel> findAllWithOwner();
}