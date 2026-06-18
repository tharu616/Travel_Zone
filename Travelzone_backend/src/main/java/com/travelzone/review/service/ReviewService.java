package com.travelzone.review.service;

import com.travelzone.common.enums.BookingStatus;
import com.travelzone.exception.*;
import com.travelzone.guide.entity.*;
import com.travelzone.guide.repository.*;
import com.travelzone.hotel.entity.*;
import com.travelzone.hotel.repository.*;
import com.travelzone.notification.service.NotificationService;
import com.travelzone.review.dto.*;
import com.travelzone.review.entity.Review;
import com.travelzone.review.repository.ReviewRepository;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GuideBookingRepository guideBookingRepository;
    private final GuideProfileRepository guideProfileRepository;
    private final ReservationRepository reservationRepository;
    private final HotelRepository hotelRepository;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         GuideBookingRepository guideBookingRepository,
                         GuideProfileRepository guideProfileRepository,
                         ReservationRepository reservationRepository,
                         HotelRepository hotelRepository,
                         NotificationService notificationService) {
        this.reviewRepository       = reviewRepository;
        this.userRepository         = userRepository;
        this.guideBookingRepository = guideBookingRepository;
        this.guideProfileRepository = guideProfileRepository;
        this.reservationRepository  = reservationRepository;
        this.hotelRepository        = hotelRepository;
        this.notificationService    = notificationService;
    }

    // ── Create review ─────────────────────────────────────────────────────────
    public ReviewResponse createReview(CreateReviewRequest request, String touristEmail) {
        User tourist = findUser(touristEmail);

        if (request.getGuideBookingId() == null && request.getReservationId() == null)
            throw new BadRequestException("Provide either guideBookingId or reservationId");
        if (request.getGuideBookingId() != null && request.getReservationId() != null)
            throw new BadRequestException("Provide only one of guideBookingId or reservationId");

        Review review = new Review();
        review.setTourist(tourist);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        if (request.getGuideBookingId() != null) {
            GuideBooking booking = guideBookingRepository.findById(request.getGuideBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guide booking not found"));
            if (!booking.getTourist().getId().equals(tourist.getId()))
                throw new UnauthorizedException("You can only review your own bookings");
            if (booking.getStatus() != BookingStatus.COMPLETED)
                throw new BadRequestException("You can only review a COMPLETED booking");
            reviewRepository.findByTouristIdAndGuideBookingId(tourist.getId(), request.getGuideBookingId())
                    .ifPresent(r -> { throw new BadRequestException("You already reviewed this booking"); });
            review.setGuideBookingId(request.getGuideBookingId());

            Review saved = reviewRepository.save(review);
            recalculateRatings(saved);

            // Notify guide — new review received
            notificationService.send(
                    booking.getGuideProfile().getUser(),
                    "New Review Received",
                    tourist.getName() + " left you a " + request.getRating()
                            + "-star review for your booking on " + booking.getBookingDate() + ".",
                    "REVIEW"
            );

            return mapToResponse(saved);

        } else {
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            if (!reservation.getTourist().getId().equals(tourist.getId()))
                throw new UnauthorizedException("You can only review your own reservations");
            if (reservation.getStatus() != BookingStatus.COMPLETED)
                throw new BadRequestException("You can only review a COMPLETED reservation");
            reviewRepository.findByTouristIdAndReservationId(tourist.getId(), request.getReservationId())
                    .ifPresent(r -> { throw new BadRequestException("You already reviewed this reservation"); });
            review.setReservationId(request.getReservationId());

            Review saved = reviewRepository.save(review);
            recalculateRatings(saved);

            // Notify hotel owner — new review received
            notificationService.send(
                    reservation.getHotel().getOwner(),
                    "New Review Received",
                    tourist.getName() + " left a " + request.getRating()
                            + "-star review for their stay at " + reservation.getHotel().getName() + ".",
                    "REVIEW"
            );

            return mapToResponse(saved);
        }
    }

    // ── Update review ─────────────────────────────────────────────────────────
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, String touristEmail) {
        User tourist = findUser(touristEmail);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getTourist().getId().equals(tourist.getId()))
            throw new UnauthorizedException("You can only update your own reviews");
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        Review saved = reviewRepository.save(review);
        recalculateRatings(saved);
        return mapToResponse(saved);
    }

    // ── Delete review ─────────────────────────────────────────────────────────
    public void deleteReview(Long reviewId, String touristEmail) {
        User tourist = findUser(touristEmail);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getTourist().getId().equals(tourist.getId()))
            throw new UnauthorizedException("You can only delete your own reviews");
        Long gbId  = review.getGuideBookingId();
        Long resId = review.getReservationId();
        reviewRepository.delete(review);
        if (gbId  != null) recalculateGuideRating(gbId);
        if (resId != null) recalculateHotelRating(resId);
    }

    // ── Guide: view received reviews ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewResponse> getGuideReviews(String guideEmail) {
        User guide = findUser(guideEmail);
        List<Long> bookingIds = guideBookingRepository
                .findByGuideProfileUserIdOrderByCreatedAtDesc(guide.getId())
                .stream().map(GuideBooking::getId).collect(Collectors.toList());
        if (bookingIds.isEmpty()) return List.of();
        return reviewRepository.findByGuideBookingIdInOrderByCreatedAtDesc(bookingIds)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Hotel owner: view received reviews ────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewResponse> getHotelOwnerReviews(String ownerEmail) {
        User owner = findUser(ownerEmail);
        List<Long> reservationIds = reservationRepository
                .findByHotelOwnerOrderByCreatedAtDesc(owner)
                .stream().map(Reservation::getId).collect(Collectors.toList());
        if (reservationIds.isEmpty()) return List.of();
        return reviewRepository.findByReservationIdInOrderByCreatedAtDesc(reservationIds)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Tourist: view own reviews ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(String touristEmail) {
        User tourist = findUser(touristEmail);
        return reviewRepository.findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Tourist: completed guide bookings grouped into trips ──────────────────
    @Transactional(readOnly = true)
    public List<ReviewableItemResponse> getReviewableGuideBookings(String touristEmail) {
        User tourist = findUser(touristEmail);

        List<GuideBooking> completed = guideBookingRepository
                .findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .sorted(Comparator.comparing(GuideBooking::getCreatedAt)
                        .thenComparing(GuideBooking::getBookingDate))
                .collect(Collectors.toList());

        Set<Long> reviewedIds = reviewRepository
                .findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream()
                .filter(r -> r.getGuideBookingId() != null)
                .map(Review::getGuideBookingId)
                .collect(Collectors.toSet());

        List<ReviewableItemResponse> groups = new ArrayList<>();
        GuideBooking groupStart = null;
        GuideBooking groupEnd   = null;

        for (GuideBooking b : completed) {
            boolean withinWindow = groupStart != null
                    && groupStart.getGuideProfile().getId().equals(b.getGuideProfile().getId())
                    && Math.abs(Duration.between(
                            groupStart.getCreatedAt(), b.getCreatedAt()).toSeconds()) < 15;

            if (withinWindow) {
                if (b.getBookingDate().isAfter(groupEnd.getBookingDate())) {
                    groupEnd = b;
                }
            } else {
                if (groupStart != null) {
                    boolean reviewed = reviewedIds.contains(groupStart.getId());
                    groups.add(new ReviewableItemResponse(
                            groupStart.getId(),
                            "GUIDE_BOOKING",
                            groupStart.getGuideProfile().getUser().getName(),
                            groupStart.getGuideProfile().getLocation(),
                            groupStart.getBookingDate(),
                            groupEnd.getBookingDate(),
                            reviewed
                    ));
                }
                groupStart = b;
                groupEnd   = b;
            }
        }

        if (groupStart != null) {
            boolean reviewed = reviewedIds.contains(groupStart.getId());
            groups.add(new ReviewableItemResponse(
                    groupStart.getId(),
                    "GUIDE_BOOKING",
                    groupStart.getGuideProfile().getUser().getName(),
                    groupStart.getGuideProfile().getLocation(),
                    groupStart.getBookingDate(),
                    groupEnd.getBookingDate(),
                    reviewed
            ));
        }

        return groups;
    }

    // ── Tourist: completed hotel reservations not yet reviewed ────────────────
    @Transactional(readOnly = true)
    public List<ReviewableItemResponse> getReviewableReservations(String touristEmail) {
        User tourist = findUser(touristEmail);

        Set<Long> reviewedIds = reviewRepository
                .findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream()
                .filter(r -> r.getReservationId() != null)
                .map(Review::getReservationId)
                .collect(Collectors.toSet());

        return reservationRepository
                .findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream()
                .filter(r -> r.getStatus() == BookingStatus.COMPLETED)
                .map(r -> new ReviewableItemResponse(
                        r.getId(),
                        "HOTEL_RESERVATION",
                        r.getHotel().getName(),
                        r.getHotel().getLocation(),
                        r.getCheckIn(),
                        r.getCheckOut(),
                        reviewedIds.contains(r.getId())
                ))
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private void recalculateRatings(Review review) {
        if (review.getGuideBookingId() != null)
            recalculateGuideRating(review.getGuideBookingId());
        else if (review.getReservationId() != null)
            recalculateHotelRating(review.getReservationId());
    }

    private void recalculateGuideRating(Long guideBookingId) {
        guideBookingRepository.findById(guideBookingId).ifPresent(booking -> {
            List<Long> bookingIds = guideBookingRepository
                    .findByGuideProfileUserIdOrderByCreatedAtDesc(
                            booking.getGuideProfile().getUser().getId())
                    .stream().map(GuideBooking::getId).collect(Collectors.toList());
            Double avg = reviewRepository.averageRatingForGuide(bookingIds);
            guideProfileRepository.findByUserId(booking.getGuideProfile().getUser().getId())
                    .ifPresent(p -> {
                        p.setRating(avg != null ? avg : 0.0);
                        guideProfileRepository.save(p);
                    });
        });
    }

    private void recalculateHotelRating(Long reservationId) {
        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            Hotel hotel = reservation.getHotel();
            List<Long> resIds = reservationRepository
                    .findByHotelOrderByCreatedAtDesc(hotel)
                    .stream().map(Reservation::getId).collect(Collectors.toList());
            Double avg = reviewRepository.averageRatingForHotel(resIds);
            hotel.setRating(avg != null ? avg : 0.0);
            hotelRepository.save(hotel);
        });
    }

    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ReviewResponse mapToResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getTourist().getId(),
                r.getTourist().getName(),
                r.getGuideBookingId(),
                r.getReservationId(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}