package com.travelzone.admin.controller;

import com.travelzone.admin.dto.*;
import com.travelzone.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserDTO> updateUserRole(
            @PathVariable Long id, @RequestBody RoleUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateUserRole(id, req.getRole()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/guides")
    public ResponseEntity<List<AdminGuideDTO>> getAllGuides() {
        return ResponseEntity.ok(adminService.getAllGuides());
    }

    @PutMapping("/guides/{id}/status")
    public ResponseEntity<AdminGuideDTO> updateGuideStatus(
            @PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateGuideStatus(id, req.getStatus()));
    }

    @DeleteMapping("/guides/{id}")
    public ResponseEntity<Void> deleteGuide(@PathVariable Long id) {
        adminService.deleteGuide(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotels")
    public ResponseEntity<List<AdminHotelDTO>> getAllHotels() {
        return ResponseEntity.ok(adminService.getAllHotels());
    }

    @PutMapping("/hotels/{id}/status")
    public ResponseEntity<AdminHotelDTO> updateHotelStatus(
            @PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateHotelStatus(id, req.getStatus()));
    }

    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        adminService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingDTO>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        adminService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<AdminReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(adminService.getAllReservations());
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        adminService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<AdminReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(adminService.getAllReviews());
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
