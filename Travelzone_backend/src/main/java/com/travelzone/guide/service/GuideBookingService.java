package com.travelzone.guide.service;

import com.travelzone.common.enums.BookingStatus;
import com.travelzone.common.enums.Role;
import com.travelzone.exception.BadRequestException;
import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.exception.UnauthorizedException;
import com.travelzone.guide.dto.CreateGuideBookingRequest;
import com.travelzone.guide.dto.GuideBookingResponse;
import com.travelzone.guide.dto.UpdateGuideBookingStatusRequest;
import com.travelzone.guide.entity.GuideAvailability;
import com.travelzone.guide.entity.GuideBooking;
import com.travelzone.guide.entity.GuideProfile;
import com.travelzone.guide.repository.GuideAvailabilityRepository;
import com.travelzone.guide.repository.GuideBookingRepository;
import com.travelzone.guide.repository.GuideProfileRepository;
import com.travelzone.notification.service.NotificationService;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GuideBookingService {

    private final GuideBookingRepository guideBookingRepository;
    private final GuideProfileRepository guideProfileRepository;
    private final GuideAvailabilityRepository guideAvailabilityRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public GuideBookingService(GuideBookingRepository guideBookingRepository,
                               GuideProfileRepository guideProfileRepository,
                               GuideAvailabilityRepository guideAvailabilityRepository,
                               UserRepository userRepository,
                               NotificationService notificationService) {
        this.guideBookingRepository = guideBookingRepository;
        this.guideProfileRepository = guideProfileRepository;
        this.guideAvailabilityRepository = guideAvailabilityRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ─── Tourist: create booking (single day OR date range) ──────────────────
    public List<GuideBookingResponse> createBooking(CreateGuideBookingRequest request,
                                                     String requesterEmail) {
        User tourist = findUser(requesterEmail);

        if (tourist.getRole() != Role.TOURIST) {
            throw new UnauthorizedException("Only tourists can create guide bookings");
        }

        LocalDate start = request.resolvedStartDate();
        LocalDate end   = request.resolvedEndDate();

        if (start == null) {
            throw new BadRequestException("Start date is required");
        }
        if (end == null) {
            end = start;
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("End date cannot be before start date");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        long days = start.datesUntil(end.plusDays(1)).count();
        if (days > 30) {
            throw new BadRequestException("Booking range cannot exceed 30 days");
        }

        GuideProfile guideProfile = guideProfileRepository.findById(request.getGuideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide profile not found"));

        List<LocalDate> dateRange = start.datesUntil(end.plusDays(1)).toList();
        List<GuideAvailability> availabilities = new ArrayList<>();

        for (LocalDate date : dateRange) {
            GuideAvailability availability = guideAvailabilityRepository
                    .findByGuideProfileAndAvailableDate(guideProfile, date)
                    .orElseThrow(() -> new BadRequestException(
                            "Guide is not available on " + date));
            if (!availability.isAvailable()) {
                throw new BadRequestException("Guide is not available on " + date);
            }
            availabilities.add(availability);
        }

        List<GuideBookingResponse> responses = new ArrayList<>();

        for (int i = 0; i < dateRange.size(); i++) {
            LocalDate date = dateRange.get(i);

            GuideBooking booking = new GuideBooking();
            booking.setGuideProfile(guideProfile);
            booking.setTourist(tourist);
            booking.setBookingDate(date);
            booking.setTotalPrice(request.getTotalPrice());
            booking.setStatus(BookingStatus.PENDING);

            GuideBooking saved = guideBookingRepository.save(booking);
            responses.add(map(saved, end));

            GuideAvailability avail = availabilities.get(i);
            avail.setAvailable(false);
            guideAvailabilityRepository.save(avail);
        }

        String rangeLabel = days == 1
                ? start.toString()
                : start + " to " + end;

        // Notify tourist — booking request sent
        notificationService.send(
                tourist,
                "Booking Request Sent",
                "Your booking request for " + rangeLabel + " has been sent to "
                        + guideProfile.getUser().getName() + ".",
                "BOOKING"
        );

        // ✅ NEW — Notify guide — new booking request received
        notificationService.send(
                guideProfile.getUser(),
                "New Booking Request",
                tourist.getName() + " has requested a booking for " + rangeLabel + ".",
                "BOOKING"
        );

        return responses;
    }

    // ─── Tourist: view own bookings ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<GuideBookingResponse> getMyBookings(String requesterEmail) {
        User tourist = findUser(requesterEmail);
        return guideBookingRepository.findByTouristOrderByCreatedAtDesc(tourist)
                .stream().map(b -> map(b, b.getBookingDate())).toList();
    }

    // ─── Guide: view incoming requests ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<GuideBookingResponse> getMyRequests(String requesterEmail) {
        User user = findUser(requesterEmail);
        return guideProfileRepository.findByUser(user)
                .map(profile -> guideBookingRepository
                        .findByGuideProfileOrderByCreatedAtDesc(profile)
                        .stream().map(b -> map(b, b.getBookingDate())).toList())
                .orElse(List.of());
    }

    // ─── Guide: accept / reject / complete ────────────────────────────────────
    public GuideBookingResponse updateBookingStatus(Long bookingId,
                                                    UpdateGuideBookingStatusRequest request,
                                                    String requesterEmail) {
        User requester = findUser(requesterEmail);

        GuideBooking booking = guideBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getGuideProfile().getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedException("Only the assigned guide can update booking status");
        }

        BookingStatus current   = booking.getStatus();
        BookingStatus newStatus = request.getStatus();

        if (current == BookingStatus.PENDING) {
            if (newStatus != BookingStatus.CONFIRMED && newStatus != BookingStatus.REJECTED) {
                throw new BadRequestException("A pending booking can only be CONFIRMED or REJECTED");
            }
        } else if (current == BookingStatus.CONFIRMED) {
            if (newStatus != BookingStatus.COMPLETED) {
                throw new BadRequestException("A confirmed booking can only be marked as COMPLETED");
            }
        } else {
            throw new BadRequestException(
                    "Cannot update a " + current.name().toLowerCase() + " booking");
        }

        booking.setStatus(newStatus);
        GuideBooking updated = guideBookingRepository.save(booking);

        if (newStatus == BookingStatus.REJECTED) {
            guideAvailabilityRepository
                    .findByGuideProfileAndAvailableDate(booking.getGuideProfile(), booking.getBookingDate())
                    .ifPresent(a -> { a.setAvailable(true); guideAvailabilityRepository.save(a); });
        }

        // Notify tourist of status change
        String title = switch (newStatus) {
            case CONFIRMED -> "Booking Confirmed";
            case REJECTED  -> "Booking Rejected";
            case COMPLETED -> "Booking Completed";
            default        -> "Booking Updated";
        };
        String message = switch (newStatus) {
            case CONFIRMED -> "Your guide booking for " + booking.getBookingDate() + " has been confirmed!";
            case REJECTED  -> "Your guide booking for " + booking.getBookingDate() + " has been rejected.";
            case COMPLETED -> "Your guide booking for " + booking.getBookingDate()
                              + " is complete. You can now leave a review!";
            default        -> "Your guide booking status updated to " + newStatus.name();
        };
        notificationService.send(booking.getTourist(), title, message, "BOOKING");

        return map(updated, updated.getBookingDate());
    }

    // ─── Tourist or guide: cancel ─────────────────────────────────────────────
    public String cancelBooking(Long bookingId, String requesterEmail) {
        User requester = findUser(requesterEmail);

        GuideBooking booking = guideBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isTourist = booking.getTourist().getId().equals(requester.getId());
        boolean isGuide   = booking.getGuideProfile().getUser().getId().equals(requester.getId());

        if (!isTourist && !isGuide) {
            throw new UnauthorizedException("Only the tourist or guide can cancel this booking");
        }

        BookingStatus current = booking.getStatus();
        if (current == BookingStatus.CANCELLED
                || current == BookingStatus.COMPLETED
                || current == BookingStatus.REJECTED) {
            throw new BadRequestException(
                    "Cannot cancel a " + current.name().toLowerCase() + " booking");
        }

        if (LocalDateTime.now().plusHours(24).toLocalDate().isAfter(booking.getBookingDate())) {
            throw new BadRequestException("Cancellation allowed only 24 hours before booking date");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        guideBookingRepository.save(booking);

        guideAvailabilityRepository
                .findByGuideProfileAndAvailableDate(booking.getGuideProfile(), booking.getBookingDate())
                .ifPresent(a -> { a.setAvailable(true); guideAvailabilityRepository.save(a); });

        // ✅ NEW — Notify both parties on cancellation
        if (isTourist) {
            // Tourist cancelled → notify the guide
            notificationService.send(
                    booking.getGuideProfile().getUser(),
                    "Booking Cancelled",
                    booking.getTourist().getName() + " has cancelled their booking for "
                            + booking.getBookingDate() + ".",
                    "BOOKING"
            );
        } else {
            // Guide cancelled → notify the tourist
            notificationService.send(
                    booking.getTourist(),
                    "Booking Cancelled",
                    "Your guide " + booking.getGuideProfile().getUser().getName()
                            + " has cancelled your booking for " + booking.getBookingDate() + ".",
                    "BOOKING"
            );
        }

        return "Guide booking cancelled successfully";
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private GuideBookingResponse map(GuideBooking b, LocalDate endDate) {
        return new GuideBookingResponse(
                b.getId(),
                b.getGuideProfile().getId(),
                b.getGuideProfile().getUser().getName(),
                b.getGuideProfile().getProfilePhoto(),
                b.getTourist().getId(),
                b.getTourist().getName(),
                b.getBookingDate(),
                endDate,
                b.getTotalPrice(),
                b.getStatus(),
                b.getCreatedAt()
        );
    }
}