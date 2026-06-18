package com.travelzone.guide.controller;

import com.travelzone.common.dto.ApiResponse;
import com.travelzone.guide.dto.CreateGuideBookingRequest;
import com.travelzone.guide.dto.GuideBookingResponse;
import com.travelzone.guide.dto.UpdateGuideBookingStatusRequest;
import com.travelzone.guide.service.GuideBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guide-bookings")
public class GuideBookingController {

    private final GuideBookingService guideBookingService;

    public GuideBookingController(GuideBookingService guideBookingService) {
        this.guideBookingService = guideBookingService;
    }

    // Tourist creates a booking (single day OR multi-day range)
    @PostMapping
    public ResponseEntity<List<GuideBookingResponse>> createBooking(
            @Valid @RequestBody CreateGuideBookingRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guideBookingService.createBooking(request, authentication.getName()));
    }

    // Tourist views their own bookings
    @GetMapping("/my-bookings")
    public ResponseEntity<List<GuideBookingResponse>> getMyBookings(Authentication authentication) {
        return ResponseEntity.ok(guideBookingService.getMyBookings(authentication.getName()));
    }

    // Guide views incoming booking requests
    @GetMapping("/my-requests")
    public ResponseEntity<List<GuideBookingResponse>> getMyRequests(Authentication authentication) {
        return ResponseEntity.ok(guideBookingService.getMyRequests(authentication.getName()));
    }

    // Guide accepts, rejects, or marks as completed
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<GuideBookingResponse> updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody UpdateGuideBookingStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                guideBookingService.updateBookingStatus(bookingId, request, authentication.getName()));
    }

    // Tourist or guide cancels a booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse(true,
                guideBookingService.cancelBooking(bookingId, authentication.getName())));
    }
}