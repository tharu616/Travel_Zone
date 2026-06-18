package com.travelzone.payment.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.travelzone.common.enums.BookingStatus;
import com.travelzone.common.enums.PaymentStatus;
import com.travelzone.common.enums.PaymentType;
import com.travelzone.exception.BadRequestException;
import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.exception.UnauthorizedException;
import com.travelzone.guide.entity.GuideBooking;
import com.travelzone.guide.repository.GuideBookingRepository;
import com.travelzone.hotel.entity.Reservation;
import com.travelzone.hotel.repository.ReservationRepository;
import com.travelzone.notification.service.NotificationService;
import com.travelzone.payment.dto.*;
import com.travelzone.payment.entity.Payment;
import com.travelzone.payment.repository.PaymentRepository;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository      paymentRepository;
    private final UserRepository         userRepository;
    private final GuideBookingRepository guideBookingRepository;
    private final ReservationRepository  reservationRepository;
    private final NotificationService    notificationService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    public PaymentService(PaymentRepository paymentRepository,
                          UserRepository userRepository,
                          GuideBookingRepository guideBookingRepository,
                          ReservationRepository reservationRepository,
                          NotificationService notificationService) {
        this.paymentRepository      = paymentRepository;
        this.userRepository         = userRepository;
        this.guideBookingRepository = guideBookingRepository;
        this.reservationRepository  = reservationRepository;
        this.notificationService    = notificationService;
    }

    // ── Step 1: Create Stripe PaymentIntent ───────────────────────────────────
    public PaymentIntentResponse createPaymentIntent(
            CreatePaymentIntentRequest request, String touristEmail) {

        User tourist = findUser(touristEmail);
        validateBookingOwnershipAndStatus(request.getPaymentType(),
                request.getReferenceId(), tourist);

        Stripe.apiKey = stripeSecretKey.trim();

        long amountInCents = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("lkr")
                    .setDescription("TravelZone - " + request.getPaymentType().name()
                            + " #" + request.getReferenceId())
                    .putMetadata("touristId",   String.valueOf(tourist.getId()))
                    .putMetadata("paymentType", request.getPaymentType().name())
                    .putMetadata("referenceId", String.valueOf(request.getReferenceId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true).build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentIntentResponse(intent.getClientSecret(), intent.getId());

        } catch (StripeException e) {
            throw new BadRequestException("Stripe error: " + e.getMessage());
        }
    }

    // ── Step 2: Record payment after Stripe confirms on frontend ─────────────
    public PaymentResponse confirmPayment(
            ConfirmPaymentRequest request, String touristEmail) {

        User tourist = findUser(touristEmail);

        Stripe.apiKey = stripeSecretKey.trim();
        try {
            PaymentIntent intent = PaymentIntent.retrieve(request.getPaymentIntentId());
            if (!"succeeded".equals(intent.getStatus()))
                throw new BadRequestException(
                        "Payment not confirmed by Stripe. Status: " + intent.getStatus());
        } catch (StripeException e) {
            throw new BadRequestException("Could not verify payment: " + e.getMessage());
        }

        if (paymentRepository.existsByPaymentTypeAndReferenceId(
                request.getPaymentType(), request.getReferenceId()))
            throw new BadRequestException("Payment already recorded for this booking.");

        Payment payment = new Payment();
        payment.setTourist(tourist);
        payment.setPaymentType(request.getPaymentType());
        payment.setReferenceId(request.getReferenceId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setTransactionNote("Stripe PaymentIntent: " + request.getPaymentIntentId()
                + (request.getTransactionNote() != null
                        ? " | " + request.getTransactionNote() : ""));

        PaymentResponse response = mapToResponse(paymentRepository.save(payment));

        // ── Notify tourist ────────────────────────────────────────────────────
        try {
            notificationService.send(tourist,
                    "Payment Successful 💳",
                    "Your " + formatType(request.getPaymentType())
                            + " payment of LKR " + request.getAmount()
                            + " was completed successfully.",
                    "PAYMENT");
        } catch (Exception e) {
            System.err.println("[PaymentService] Tourist notification failed: " + e.getMessage());
        }

        // ── Notify guide or hotel owner ───────────────────────────────────────
        try {
            if (request.getPaymentType() == PaymentType.GUIDE_BOOKING) {
                guideBookingRepository.findById(request.getReferenceId()).ifPresent(booking -> {
                    notificationService.send(booking.getGuideProfile().getUser(),
                            "Payment Received 💰",
                            tourist.getName() + " paid LKR " + request.getAmount()
                                    + " for guide booking #" + request.getReferenceId() + ".",
                            "PAYMENT");
                });
            } else if (request.getPaymentType() == PaymentType.HOTEL_RESERVATION) {
                reservationRepository.findById(request.getReferenceId()).ifPresent(reservation -> {
                    notificationService.send(reservation.getHotel().getOwner(),
                            "Payment Received 💰",
                            tourist.getName() + " paid LKR " + request.getAmount()
                                    + " for reservation #" + request.getReferenceId()
                                    + " at " + reservation.getHotel().getName() + ".",
                            "PAYMENT");
                });
            }
        } catch (Exception e) {
            System.err.println("[PaymentService] Owner notification failed: " + e.getMessage());
        }

        return response;
    }

    // ── Legacy: Cash / Bank Transfer / PayPal (no Stripe) ────────────────────
    public PaymentResponse makePayment(CreatePaymentRequest request, String touristEmail) {
        User tourist = findUser(touristEmail);

        if (request.getPaymentType() == PaymentType.GUIDE_BOOKING) {
            GuideBooking booking = guideBookingRepository.findById(request.getReferenceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guide booking not found"));
            if (!booking.getTourist().getId().equals(tourist.getId()))
                throw new UnauthorizedException("You can only pay for your own bookings");
            if (booking.getStatus() != BookingStatus.CONFIRMED)
                throw new BadRequestException("Booking must be CONFIRMED before payment");
            if (paymentRepository.existsByPaymentTypeAndReferenceId(
                    PaymentType.GUIDE_BOOKING, request.getReferenceId()))
                throw new BadRequestException("Payment already exists for this booking");

        } else if (request.getPaymentType() == PaymentType.HOTEL_RESERVATION) {
            Reservation reservation = reservationRepository.findById(request.getReferenceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            if (!reservation.getTourist().getId().equals(tourist.getId()))
                throw new UnauthorizedException("You can only pay for your own reservations");
            if (reservation.getStatus() != BookingStatus.CONFIRMED)
                throw new BadRequestException("Reservation must be CONFIRMED before payment");
            if (paymentRepository.existsByPaymentTypeAndReferenceId(
                    PaymentType.HOTEL_RESERVATION, request.getReferenceId()))
                throw new BadRequestException("Payment already exists for this reservation");
        }

        Payment payment = new Payment();
        payment.setTourist(tourist);
        payment.setPaymentType(request.getPaymentType());
        payment.setReferenceId(request.getReferenceId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionNote(request.getTransactionNote());

        PaymentResponse response = mapToResponse(paymentRepository.save(payment));

        // ── Notify tourist ────────────────────────────────────────────────────
        try {
            notificationService.send(tourist,
                    "Payment Recorded ✅",
                    "Your " + request.getPaymentMethod().replace("_", " ")
                            + " payment of LKR " + request.getAmount()
                            + " for " + formatType(request.getPaymentType())
                            + " #" + request.getReferenceId() + " has been recorded.",
                    "PAYMENT");
        } catch (Exception e) {
            System.err.println("[PaymentService] Tourist notification failed: " + e.getMessage());
        }

        // ── Notify guide or hotel owner ───────────────────────────────────────
        try {
            if (request.getPaymentType() == PaymentType.GUIDE_BOOKING) {
                guideBookingRepository.findById(request.getReferenceId()).ifPresent(booking -> {
                    notificationService.send(booking.getGuideProfile().getUser(),
                            "Payment Received 💰",
                            tourist.getName() + " sent a "
                                    + request.getPaymentMethod().replace("_", " ")
                                    + " payment of LKR " + request.getAmount()
                                    + " for booking #" + request.getReferenceId() + ".",
                            "PAYMENT");
                });
            } else if (request.getPaymentType() == PaymentType.HOTEL_RESERVATION) {
                reservationRepository.findById(request.getReferenceId()).ifPresent(reservation -> {
                    notificationService.send(reservation.getHotel().getOwner(),
                            "Payment Received 💰",
                            tourist.getName() + " sent a "
                                    + request.getPaymentMethod().replace("_", " ")
                                    + " payment of LKR " + request.getAmount()
                                    + " for reservation #" + request.getReferenceId()
                                    + " at " + reservation.getHotel().getName() + ".",
                            "PAYMENT");
                });
            }
        } catch (Exception e) {
            System.err.println("[PaymentService] Owner notification failed: " + e.getMessage());
        }

        return response;
    }

    // ── Read-only queries ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String touristEmail) {
        User tourist = findUser(touristEmail);
        return paymentRepository.findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getGuidePayments(String guideEmail) {
        User guide = findUser(guideEmail);
        List<Long> bookingIds = guideBookingRepository
                .findByGuideProfileUserIdOrderByCreatedAtDesc(guide.getId())
                .stream().map(GuideBooking::getId).collect(Collectors.toList());
        if (bookingIds.isEmpty()) return List.of();
        return paymentRepository
                .findByPaymentTypeAndReferenceIdInOrderByCreatedAtDesc(
                        PaymentType.GUIDE_BOOKING, bookingIds)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getHotelOwnerPayments(String ownerEmail) {
        User owner = findUser(ownerEmail);
        List<Long> reservationIds = reservationRepository
                .findByHotelOwnerOrderByCreatedAtDesc(owner)
                .stream().map(Reservation::getId).collect(Collectors.toList());
        if (reservationIds.isEmpty()) return List.of();
        return paymentRepository
                .findByPaymentTypeAndReferenceIdInOrderByCreatedAtDesc(
                        PaymentType.HOTEL_RESERVATION, reservationIds)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private void validateBookingOwnershipAndStatus(
            PaymentType type, Long referenceId, User tourist) {

        if (type == PaymentType.GUIDE_BOOKING) {
            GuideBooking booking = guideBookingRepository.findById(referenceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guide booking not found"));
            if (!booking.getTourist().getId().equals(tourist.getId()))
                throw new UnauthorizedException("Not your booking");
            if (booking.getStatus() != BookingStatus.CONFIRMED)
                throw new BadRequestException("Booking must be CONFIRMED before payment");
            if (paymentRepository.existsByPaymentTypeAndReferenceId(type, referenceId))
                throw new BadRequestException("Payment already exists for this booking");

        } else if (type == PaymentType.HOTEL_RESERVATION) {
            Reservation reservation = reservationRepository.findById(referenceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            if (!reservation.getTourist().getId().equals(tourist.getId()))
                throw new UnauthorizedException("Not your reservation");
            if (reservation.getStatus() != BookingStatus.CONFIRMED)
                throw new BadRequestException("Reservation must be CONFIRMED before payment");
            if (paymentRepository.existsByPaymentTypeAndReferenceId(type, referenceId))
                throw new BadRequestException("Payment already exists for this reservation");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String formatType(PaymentType type) {
        return type.name().replace("_", " ").toLowerCase();
    }

    private PaymentResponse mapToResponse(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getTourist().getId(), p.getTourist().getName(),
                p.getPaymentType(), p.getReferenceId(), p.getAmount(),
                p.getStatus(), p.getPaymentMethod(), p.getTransactionNote(), p.getCreatedAt());
    }
}