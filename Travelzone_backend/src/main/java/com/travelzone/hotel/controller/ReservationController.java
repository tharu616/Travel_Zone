package com.travelzone.hotel.controller;

import com.travelzone.common.dto.ApiResponse;
import com.travelzone.hotel.dto.CreateReservationRequest;
import com.travelzone.hotel.dto.ReservationResponse;
import com.travelzone.hotel.dto.UpdateReservationStatusRequest;
import com.travelzone.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Tourist: create reservation
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request, authentication.getName()));
    }

    // Tourist: view own reservations
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        return ResponseEntity.ok(reservationService.getMyReservations(authentication.getName()));
    }

    // Hotel owner: view reservations for their hotels
    @GetMapping("/my-hotel-reservations")
    public ResponseEntity<List<ReservationResponse>> getMyHotelReservations(Authentication authentication) {
        return ResponseEntity.ok(reservationService.getMyHotelReservations(authentication.getName()));
    }

    // ✅ Hotel owner: update reservation status (CONFIRMED / COMPLETED / REJECTED)
    @PutMapping("/{reservationId}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long reservationId,
            @Valid @RequestBody UpdateReservationStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                reservationService.updateReservationStatus(reservationId, request, authentication.getName()));
    }

    // Tourist: cancel reservation
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse> cancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse(true,
                reservationService.cancelReservation(reservationId, authentication.getName())));
    }
}
