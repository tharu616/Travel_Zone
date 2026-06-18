package com.travelzone.guide.dto;

import com.travelzone.common.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateGuideBookingStatusRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;

    public UpdateGuideBookingStatusRequest() {
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
