package com.travelzone.hotel.service;

import com.travelzone.common.enums.BookingStatus;
import com.travelzone.common.enums.Role;
import com.travelzone.exception.BadRequestException;
import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.exception.UnauthorizedException;
import com.travelzone.hotel.dto.CreateReservationRequest;
import com.travelzone.hotel.dto.ReservationResponse;
import com.travelzone.hotel.dto.UpdateReservationStatusRequest;
import com.travelzone.hotel.entity.Hotel;
import com.travelzone.hotel.entity.Reservation;
import com.travelzone.hotel.entity.Room;
import com.travelzone.hotel.repository.HotelRepository;
import com.travelzone.hotel.repository.ReservationRepository;
import com.travelzone.hotel.repository.RoomRepository;
import com.travelzone.notification.service.NotificationService;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              HotelRepository hotelRepository,
                              RoomRepository roomRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ─── Tourist: create reservation ─────────────────────────────────────────
    public ReservationResponse createReservation(CreateReservationRequest request, String requesterEmail) {
        User tourist = findUser(requesterEmail);

        if (tourist.getRole() != Role.TOURIST) {
            throw new UnauthorizedException("Only tourists can make reservations");
        }

        if (request.getCheckIn().isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getId().equals(hotel.getId())) {
            throw new BadRequestException("Room does not belong to selected hotel");
        }
        if (!room.isAvailable() || room.getAvailableCount() <= 0) {
            throw new BadRequestException("Room is not available");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (nights <= 0) throw new BadRequestException("Check-out must be after check-in");

        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = new Reservation();
        reservation.setHotel(hotel);
        reservation.setRoom(room);
        reservation.setTourist(tourist);
        reservation.setCheckIn(request.getCheckIn());
        reservation.setCheckOut(request.getCheckOut());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(BookingStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);

        room.setAvailableCount(room.getAvailableCount() - 1);
        if (room.getAvailableCount() <= 0) room.setAvailable(false);
        roomRepository.save(room);

        // Notify tourist — reservation request sent
        notificationService.send(
                tourist,
                "Reservation Request Sent",
                "Your reservation request at " + hotel.getName()
                        + " (" + request.getCheckIn() + " → " + request.getCheckOut() + ") has been submitted.",
                "RESERVATION"
        );

        // ✅ NEW — Notify hotel owner — new reservation received
        notificationService.send(
                hotel.getOwner(),
                "New Reservation Request",
                tourist.getName() + " has requested a reservation at " + hotel.getName()
                        + " (" + request.getCheckIn() + " → " + request.getCheckOut() + ").",
                "RESERVATION"
        );

        return map(saved);
    }

    // ─── Tourist: view own reservations ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(String requesterEmail) {
        User tourist = findUser(requesterEmail);
        return reservationRepository.findByTouristOrderByCreatedAtDesc(tourist)
                .stream().map(this::map).toList();
    }

    // ─── Hotel owner: view reservations for their hotels ─────────────────────
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyHotelReservations(String requesterEmail) {
        User owner = findUser(requesterEmail);
        return reservationRepository.findByHotelOwnerOrderByCreatedAtDesc(owner)
                .stream().map(this::map).toList();
    }

    // ─── Hotel owner: update reservation status ───────────────────────────────
    public ReservationResponse updateReservationStatus(Long reservationId,
                                                        UpdateReservationStatusRequest request,
                                                        String requesterEmail) {
        User owner = findUser(requesterEmail);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.getHotel().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the hotel owner can update reservation status");
        }

        BookingStatus current   = reservation.getStatus();
        BookingStatus newStatus = request.getStatus();

        if (current == BookingStatus.CANCELLED
                || current == BookingStatus.COMPLETED
                || current == BookingStatus.REJECTED) {
            throw new BadRequestException(
                    "Cannot update a " + current.name().toLowerCase() + " reservation");
        }

        if (newStatus != BookingStatus.CONFIRMED
                && newStatus != BookingStatus.COMPLETED
                && newStatus != BookingStatus.REJECTED) {
            throw new BadRequestException(
                    "Hotel owner can only set: CONFIRMED, COMPLETED, or REJECTED");
        }

        if (current == BookingStatus.PENDING && newStatus == BookingStatus.COMPLETED) {
            throw new BadRequestException(
                    "Reservation must be CONFIRMED before it can be marked as COMPLETED");
        }

        if (newStatus == BookingStatus.REJECTED) {
            Room room = reservation.getRoom();
            room.setAvailableCount(room.getAvailableCount() + 1);
            room.setAvailable(true);
            roomRepository.save(room);
        }

        reservation.setStatus(newStatus);
        reservationRepository.save(reservation);

        // Notify tourist of status change
        String title = switch (newStatus) {
            case CONFIRMED -> "Reservation Confirmed";
            case REJECTED  -> "Reservation Rejected";
            case COMPLETED -> "Stay Completed";
            default        -> "Reservation Updated";
        };
        String message = switch (newStatus) {
            case CONFIRMED -> "Your reservation at " + reservation.getHotel().getName() + " has been confirmed!";
            case REJECTED  -> "Your reservation at " + reservation.getHotel().getName() + " has been rejected.";
            case COMPLETED -> "Your stay at " + reservation.getHotel().getName()
                              + " is complete. You can now leave a review!";
            default        -> "Your reservation at " + reservation.getHotel().getName()
                              + " has been " + newStatus.name().toLowerCase() + ".";
        };
        notificationService.send(reservation.getTourist(), title, message, "RESERVATION");

        return map(reservation);
    }

    // ─── Tourist: cancel reservation ─────────────────────────────────────────
    public String cancelReservation(Long reservationId, String requesterEmail) {
        User tourist = findUser(requesterEmail);

        if (tourist.getRole() != Role.TOURIST) {
            throw new UnauthorizedException("Only tourists can cancel reservations");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.getTourist().getId().equals(tourist.getId())) {
            throw new UnauthorizedException("Only the tourist can cancel this reservation");
        }

        BookingStatus current = reservation.getStatus();
        if (current == BookingStatus.CANCELLED
                || current == BookingStatus.COMPLETED
                || current == BookingStatus.REJECTED) {
            throw new BadRequestException(
                    "Cannot cancel a " + current.name().toLowerCase() + " reservation");
        }

        long hoursUntilCheckIn = java.time.LocalDateTime.now()
                .until(reservation.getCheckIn().atStartOfDay(), ChronoUnit.HOURS);

        if (hoursUntilCheckIn < 48) {
            throw new BadRequestException("Cancellation allowed only 48 hours before check-in");
        }

        reservation.setStatus(BookingStatus.CANCELLED);
        reservationRepository.save(reservation);

        Room room = reservation.getRoom();
        room.setAvailableCount(room.getAvailableCount() + 1);
        room.setAvailable(true);
        roomRepository.save(room);

        // Notify tourist — cancellation confirmed
        notificationService.send(
                tourist,
                "Reservation Cancelled",
                "Your reservation at " + reservation.getHotel().getName() + " has been cancelled.",
                "RESERVATION"
        );

        // ✅ NEW — Notify hotel owner — tourist cancelled
        notificationService.send(
                reservation.getHotel().getOwner(),
                "Reservation Cancelled",
                tourist.getName() + " has cancelled their reservation at "
                        + reservation.getHotel().getName()
                        + " (" + reservation.getCheckIn() + " → " + reservation.getCheckOut() + ").",
                "RESERVATION"
        );

        return "Reservation cancelled successfully";
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ReservationResponse map(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getHotel().getId(),
                r.getHotel().getName(),
                r.getHotel().getLocation(),
                r.getRoom().getId(),
                r.getRoom().getRoomType(),
                r.getTourist().getId(),
                r.getTourist().getName(),
                r.getCheckIn(),
                r.getCheckOut(),
                r.getTotalPrice(),
                r.getStatus(),
                r.getCreatedAt()
        );
    }
}