package com.travelzone.hotel.dto;

import com.travelzone.common.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateReservationStatusRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;

    public UpdateReservationStatusRequest() {}

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
