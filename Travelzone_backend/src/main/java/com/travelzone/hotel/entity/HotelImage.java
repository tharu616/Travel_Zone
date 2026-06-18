package com.travelzone.hotel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hotel_images")
public class HotelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    // ✅ Fixed: LONGTEXT supports Base64 image strings
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String imageUrl;

    public HotelImage() {}

    public Long getId() { return id; }
    public Hotel getHotel() { return hotel; }
    public String getImageUrl() { return imageUrl; }

    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
