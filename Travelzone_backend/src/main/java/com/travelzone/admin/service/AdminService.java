package com.travelzone.admin.service;

import com.travelzone.common.enums.BookingStatus;
import com.travelzone.common.enums.Role;

import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;

import com.travelzone.guide.entity.GuideProfile;
import com.travelzone.guide.entity.GuideBooking;
import com.travelzone.guide.repository.GuideProfileRepository;
import com.travelzone.guide.repository.GuideBookingRepository;

import com.travelzone.hotel.entity.Hotel;
import com.travelzone.hotel.entity.Reservation;
import com.travelzone.hotel.repository.HotelRepository;
import com.travelzone.hotel.repository.ReservationRepository;

import com.travelzone.payment.entity.Payment;
import com.travelzone.payment.repository.PaymentRepository;

import com.travelzone.review.entity.Review;
import com.travelzone.review.repository.ReviewRepository;

import com.travelzone.notification.repository.NotificationRepository;

import com.travelzone.admin.dto.AdminStatsDTO;
import com.travelzone.admin.dto.AdminUserDTO;
import com.travelzone.admin.dto.AdminGuideDTO;
import com.travelzone.admin.dto.AdminHotelDTO;
import com.travelzone.admin.dto.AdminBookingDTO;
import com.travelzone.admin.dto.AdminReservationDTO;
import com.travelzone.admin.dto.AdminPaymentDTO;
import com.travelzone.admin.dto.AdminReviewDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository         userRepository;
    private final GuideProfileRepository guideProfileRepository;
    private final GuideBookingRepository guideBookingRepository;
    private final HotelRepository        hotelRepository;
    private final ReservationRepository  reservationRepository;
    private final PaymentRepository      paymentRepository;
    private final ReviewRepository       reviewRepository;
    private final NotificationRepository notificationRepository;

    public AdminService(UserRepository userRepository,
                        GuideProfileRepository guideProfileRepository,
                        GuideBookingRepository guideBookingRepository,
                        HotelRepository hotelRepository,
                        ReservationRepository reservationRepository,
                        PaymentRepository paymentRepository,
                        ReviewRepository reviewRepository,
                        NotificationRepository notificationRepository) {
        this.userRepository         = userRepository;
        this.guideProfileRepository = guideProfileRepository;
        this.guideBookingRepository = guideBookingRepository;
        this.hotelRepository        = hotelRepository;
        this.reservationRepository  = reservationRepository;
        this.paymentRepository      = paymentRepository;
        this.reviewRepository       = reviewRepository;
        this.notificationRepository = notificationRepository;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AdminStatsDTO getStats() {
        double totalRevenue = paymentRepository.findAll().stream()
            .filter(p -> p.getStatus() != null &&
                    "COMPLETED".equalsIgnoreCase(p.getStatus().name()))
            .mapToDouble(p -> p.getAmount() != null
                    ? p.getAmount().doubleValue() : 0.0)
            .sum();

        long pendingBookings = guideBookingRepository.findAll().stream()
            .filter(b -> b.getStatus() == BookingStatus.PENDING).count();

        long pendingReservations = reservationRepository.findAll().stream()
            .filter(r -> r.getStatus() == BookingStatus.PENDING).count();

        return AdminStatsDTO.builder()
            .totalUsers(userRepository.count())
            .totalGuides(guideProfileRepository.count())
            .totalHotels(hotelRepository.count())
            .totalBookings(guideBookingRepository.count())
            .totalReservations(reservationRepository.count())
            .totalPayments(paymentRepository.count())
            .totalRevenue(totalRevenue)
            .pendingBookings(pendingBookings)
            .pendingReservations(pendingReservations)
            .totalReviews(reviewRepository.count())
            .build();
    }

    // ── Users ─────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> AdminUserDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole().name())
                .profilePicture(u.getProfilePicture())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserDTO updateUserRole(Long id, String role) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Role.valueOf(role));
        userRepository.save(user);
        return AdminUserDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // 1. ✅ Notifications first — direct FK to users
        notificationRepository.deleteAll(
            notificationRepository.findByUserIdOrderByCreatedAtDesc(id));

        // 2. Reviews by this user as tourist
        reviewRepository.deleteAll(
            reviewRepository.findByTouristIdOrderByCreatedAtDesc(id));

        // 3. Payments by this user as tourist
        paymentRepository.deleteAll(
            paymentRepository.findByTouristIdOrderByCreatedAtDesc(id));

        // 4. Guide bookings where this user is tourist
        guideBookingRepository.deleteAll(
            guideBookingRepository.findByTouristIdOrderByCreatedAtDesc(id));

        // 5. Hotel reservations where this user is tourist
        reservationRepository.deleteAll(
            reservationRepository.findByTouristIdOrderByCreatedAtDesc(id));

        // 6. Guide profile if this user is a guide
        guideProfileRepository.findByUser(user)
            .ifPresent(guideProfileRepository::delete);

        // 7. Hotels owned by this user
        List<Hotel> ownedHotels = hotelRepository.findByOwnerOrderByIdDesc(user);
        if (!ownedHotels.isEmpty()) {
            hotelRepository.deleteAll(ownedHotels);
        }

        // 8. Finally delete the user
        userRepository.delete(user);
    }

    // ── Guides ────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminGuideDTO> getAllGuides() {
        return guideProfileRepository.findAll().stream()
            .map(g -> AdminGuideDTO.builder()
                .id(g.getId())
                .name(g.getUser() != null ? g.getUser().getName() : "")
                .email(g.getUser() != null ? g.getUser().getEmail() : "")
                .location(g.getLocation())
                .languages(g.getLanguages() != null
                        ? String.join(", ", g.getLanguages()) : "")
                .pricePerDay(g.getPricePerDay() != null
                        ? g.getPricePerDay().doubleValue() : null)
                .status(g.getUser() != null && g.getUser().isActive()
                        ? "ACTIVE" : "INACTIVE")
                .rating(g.getRating())
                .profilePicture(g.getProfilePhoto())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminGuideDTO updateGuideStatus(Long id, String status) {
        GuideProfile guide = guideProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Guide not found"));
        if (guide.getUser() != null) {
            guide.getUser().setActive("ACTIVE".equalsIgnoreCase(status));
            userRepository.save(guide.getUser());
        }
        return AdminGuideDTO.builder()
            .id(guide.getId())
            .name(guide.getUser() != null ? guide.getUser().getName() : "")
            .status(status)
            .build();
    }

    @Transactional
    public void deleteGuide(Long id) {
        guideProfileRepository.deleteById(id);
    }

    // ── Hotels ────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminHotelDTO> getAllHotels() {
        return hotelRepository.findAllWithOwner()
            .stream()
            .map(h -> AdminHotelDTO.builder()
                .id(h.getId())
                .name(h.getName())
                .location(h.getLocation())
                .ownerName(h.getOwner() != null ? h.getOwner().getName() : "")
                .ownerEmail(h.getOwner() != null ? h.getOwner().getEmail() : "")
                .totalRooms(0)
                .status(h.isActive() ? "ACTIVE" : "INACTIVE")
                .rating(h.getRating())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminHotelDTO updateHotelStatus(Long id, String status) {
        Hotel hotel = hotelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Hotel not found"));
        hotel.setActive("ACTIVE".equalsIgnoreCase(status));
        hotelRepository.save(hotel);
        return AdminHotelDTO.builder()
            .id(hotel.getId())
            .name(hotel.getName())
            .status(hotel.isActive() ? "ACTIVE" : "INACTIVE")
            .build();
    }

    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    // ── Guide Bookings ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminBookingDTO> getAllBookings() {
        return guideBookingRepository.findAll().stream()
            .map(b -> AdminBookingDTO.builder()
                .id(b.getId())
                .touristName(b.getTourist() != null ? b.getTourist().getName() : "")
                .touristEmail(b.getTourist() != null ? b.getTourist().getEmail() : "")
                .guideName(b.getGuideProfile() != null && b.getGuideProfile().getUser() != null
                        ? b.getGuideProfile().getUser().getName() : "")
                .startDate(b.getBookingDate() != null
                        ? b.getBookingDate().atStartOfDay() : null)
                .endDate(null)
                .status(b.getStatus() != null ? b.getStatus().name() : "")
                .totalPrice(b.getTotalPrice() != null
                        ? b.getTotalPrice().doubleValue() : null)
                .createdAt(b.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBooking(Long id) {
        guideBookingRepository.deleteById(id);
    }

    // ── Reservations ──────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
            .map(r -> AdminReservationDTO.builder()
                .id(r.getId())
                .guestName(r.getTourist() != null ? r.getTourist().getName() : "")
                .guestEmail(r.getTourist() != null ? r.getTourist().getEmail() : "")
                .hotelName(r.getHotel() != null ? r.getHotel().getName() : "")
                .roomType(r.getRoom() != null ? "Room #" + r.getRoom().getId() : "")
                .checkIn(r.getCheckIn() != null ? r.getCheckIn().atStartOfDay() : null)
                .checkOut(r.getCheckOut() != null ? r.getCheckOut().atStartOfDay() : null)
                .status(r.getStatus() != null ? r.getStatus().name() : "")
                .totalPrice(r.getTotalPrice() != null
                        ? r.getTotalPrice().doubleValue() : null)
                .createdAt(r.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    // ── Payments ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminPaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
            .map(p -> AdminPaymentDTO.builder()
                .id(p.getId())
                .payerName(p.getTourist() != null ? p.getTourist().getName() : "")
                .payerEmail(p.getTourist() != null ? p.getTourist().getEmail() : "")
                .type(p.getPaymentType() != null ? p.getPaymentType().name() : "")
                .amount(p.getAmount() != null ? p.getAmount().doubleValue() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : "")
                .stripeId(p.getPaymentMethod())
                .createdAt(p.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    // ── Reviews ───────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AdminReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
            .map(r -> AdminReviewDTO.builder()
                .id(r.getId())
                .reviewerName(r.getTourist() != null ? r.getTourist().getName() : "")
                .reviewerEmail(r.getTourist() != null ? r.getTourist().getEmail() : "")
                .targetName(r.getGuideBookingId() != null
                        ? "Guide Booking #" + r.getGuideBookingId()
                        : "Reservation #" + r.getReservationId())
                .targetType(r.getGuideBookingId() != null ? "GUIDE" : "HOTEL")
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}