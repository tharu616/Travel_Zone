package com.travelzone.hotel.controller;

import com.travelzone.common.dto.ApiResponse;
import com.travelzone.hotel.dto.*;
import com.travelzone.hotel.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // Create hotel
    @PostMapping
    public ResponseEntity<HotelDetailResponse> createHotel(
            @Valid @RequestBody CreateHotelRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.createHotel(request, authentication.getName()));
    }

    // Public search
    @GetMapping
    public ResponseEntity<Page<HotelSearchResponse>> searchHotels(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) String facilities,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(hotelService.searchHotels(location, price, facilities, page, size));
    }

    // Hotel owner: view own hotels
    @GetMapping("/my-hotels")
    public ResponseEntity<List<HotelSearchResponse>> getMyHotels(Authentication authentication) {
        return ResponseEntity.ok(hotelService.getMyHotels(authentication.getName()));
    }

    // Get hotel detail
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDetailResponse> getHotelDetails(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelDetails(hotelId));
    }

    // ✅ Hotel owner: update hotel details
    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDetailResponse> updateHotel(
            @PathVariable Long hotelId,
            @Valid @RequestBody UpdateHotelRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(hotelService.updateHotel(hotelId, request, authentication.getName()));
    }

    // ✅ Hotel owner: delete hotel
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<ApiResponse> deleteHotel(
            @PathVariable Long hotelId,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse(true,
                hotelService.deleteHotel(hotelId, authentication.getName())));
    }

    // Hotel owner: add room
    @PostMapping("/{hotelId}/rooms")
    public ResponseEntity<RoomResponse> addRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateRoomRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.addRoom(hotelId, request, authentication.getName()));
    }

    // ✅ Hotel owner: delete room
    @DeleteMapping("/{hotelId}/rooms/{roomId}")
    public ResponseEntity<ApiResponse> deleteRoom(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse(true,
                hotelService.deleteRoom(hotelId, roomId, authentication.getName())));
    }
}
