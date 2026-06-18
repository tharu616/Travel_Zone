package com.travelzone.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateRoomRequest {

    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Room count is required")
    @Min(value = 1, message = "Room count must be at least 1")
    private Integer roomCount;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal pricePerNight;

    public CreateRoomRequest() {}

    public String getRoomType() { return roomType; }
    public Integer getRoomCount() { return roomCount; }
    public BigDecimal getPricePerNight() { return pricePerNight; }

    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setRoomCount(Integer roomCount) { this.roomCount = roomCount; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
}
