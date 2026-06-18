package com.travelzone.payment.controller;

import com.travelzone.payment.dto.*;
import com.travelzone.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Step 1 — Tourist creates a PaymentIntent; receives client_secret for Stripe.js */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                paymentService.createPaymentIntent(request, authentication.getName()));
    }

    /** Step 2 — Frontend confirms card via Stripe.js, then calls this to record in DB */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.confirmPayment(request, authentication.getName()));
    }

    /** Legacy direct payment (CASH / BANK_TRANSFER / PAYPAL — no card) */
    @PostMapping
    public ResponseEntity<PaymentResponse> makePayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.makePayment(request, authentication.getName()));
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getMyPayments(authentication.getName()));
    }

    @GetMapping("/guide-payments")
    public ResponseEntity<List<PaymentResponse>> getGuidePayments(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getGuidePayments(authentication.getName()));
    }

    @GetMapping("/hotel-payments")
    public ResponseEntity<List<PaymentResponse>> getHotelPayments(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getHotelOwnerPayments(authentication.getName()));
    }
} 