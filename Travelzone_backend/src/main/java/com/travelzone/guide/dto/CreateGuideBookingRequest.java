package com.travelzone.guide.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGuideBookingRequest {

    @NotNull(message = "Guide id is required")
    private Long guideId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    // ── kept for backward compatibility with old single-date clients ──────────
    private LocalDate bookingDate;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.01", message = "Total price must be positive")
    private BigDecimal totalPrice;

    public CreateGuideBookingRequest() {}

    public Long getGuideId() { return guideId; }
    public void setGuideId(Long guideId) { this.guideId = guideId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // If old client sends only bookingDate, treat it as a single-day range
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    /**
     * Resolves the effective start date:
     * new clients send startDate; old clients send bookingDate.
     */
    public LocalDate resolvedStartDate() {
        return startDate != null ? startDate : bookingDate;
    }

    /**
     * Resolves the effective end date:
     * new clients send endDate; old clients send bookingDate (single day).
     */
    public LocalDate resolvedEndDate() {
        return endDate != null ? endDate : bookingDate;
    }
}