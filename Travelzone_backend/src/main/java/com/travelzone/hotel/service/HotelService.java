package com.travelzone.hotel.service;

import com.travelzone.common.enums.Role;
import com.travelzone.exception.BadRequestException;
import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.exception.UnauthorizedException;
import com.travelzone.hotel.dto.*;
import com.travelzone.hotel.entity.Hotel;
import com.travelzone.hotel.entity.HotelImage;
import com.travelzone.hotel.entity.Room;
import com.travelzone.hotel.repository.HotelImageRepository;
import com.travelzone.hotel.repository.HotelRepository;
import com.travelzone.hotel.repository.ReservationRepository;
import com.travelzone.hotel.repository.RoomRepository;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class HotelService {

    private final HotelRepository      hotelRepository;
    private final HotelImageRepository hotelImageRepository;
    private final RoomRepository       roomRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository       userRepository;

    public HotelService(HotelRepository hotelRepository,
                        HotelImageRepository hotelImageRepository,
                        RoomRepository roomRepository,
                        ReservationRepository reservationRepository,
                        UserRepository userRepository) {
        this.hotelRepository      = hotelRepository;
        this.hotelImageRepository = hotelImageRepository;
        this.roomRepository       = roomRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository       = userRepository;
    }

    // ─── Create hotel ─────────────────────────────────────────────────────────
    public HotelDetailResponse createHotel(CreateHotelRequest request, String requesterEmail) {
        User owner = findUser(requesterEmail);
        if (owner.getRole() != Role.HOTEL_OWNER) {
            throw new UnauthorizedException("Only HOTEL_OWNER can create hotel listings");
        }

        Hotel hotel = new Hotel();
        hotel.setOwner(owner);
        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setDescription(request.getDescription());
        hotel.setFacilities(request.getFacilities());
        hotel.setActive(true);
        Hotel saved = hotelRepository.save(hotel);

        for (String imageUrl : request.getImages()) {
            HotelImage image = new HotelImage();
            image.setHotel(saved);
            image.setImageUrl(imageUrl);
            hotelImageRepository.save(image);
        }

        return getHotelDetails(saved.getId());
    }

    // ─── Update hotel ─────────────────────────────────────────────────────────
    public HotelDetailResponse updateHotel(Long hotelId, UpdateHotelRequest request, String requesterEmail) {
        User owner = findUser(requesterEmail);
        Hotel hotel = findHotel(hotelId);

        if (!hotel.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the hotel owner can update this hotel");
        }

        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setDescription(request.getDescription());
        hotel.setFacilities(request.getFacilities());
        hotelRepository.save(hotel);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            hotelImageRepository.deleteAll(hotelImageRepository.findByHotel(hotel));
            for (String imageUrl : request.getImages()) {
                HotelImage image = new HotelImage();
                image.setHotel(hotel);
                image.setImageUrl(imageUrl);
                hotelImageRepository.save(image);
            }
        }

        return getHotelDetails(hotelId);
    }

    // ─── Delete hotel ─────────────────────────────────────────────────────────
    public String deleteHotel(Long hotelId, String requesterEmail) {
        User owner = findUser(requesterEmail);
        Hotel hotel = findHotel(hotelId);

        if (!hotel.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the hotel owner can delete this hotel");
        }

        boolean hasActiveReservations = reservationRepository.findByHotelOrderByCreatedAtDesc(hotel)
                .stream()
                .anyMatch(r -> r.getStatus().name().equals("PENDING")
                        || r.getStatus().name().equals("CONFIRMED"));

        if (hasActiveReservations) {
            throw new BadRequestException("Cannot delete hotel with active reservations");
        }

        hotelImageRepository.deleteAll(hotelImageRepository.findByHotel(hotel));
        reservationRepository.deleteAll(reservationRepository.findByHotelOrderByCreatedAtDesc(hotel));
        roomRepository.deleteAll(roomRepository.findByHotelId(hotelId));
        hotelRepository.delete(hotel);

        return "Hotel deleted successfully";
    }

    // ─── Owner's hotels ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<HotelSearchResponse> getMyHotels(String requesterEmail) {
        User owner = findUser(requesterEmail);
        return hotelRepository.findByOwnerOrderByIdDesc(owner).stream()
                .map(hotel -> {
                    List<HotelImage> images = hotelImageRepository.findByHotel(hotel);
                    String thumbnail = images.isEmpty() ? null : images.get(0).getImageUrl();
                    return new HotelSearchResponse(
                            hotel.getId(), hotel.getName(), hotel.getLocation(),
                            thumbnail, hotel.getRating(), hotel.getMinPrice());
                }).toList();
    }

    // ─── Public search ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<HotelSearchResponse> searchHotels(String location, BigDecimal price,
                                                   String facilities, int page, int size) {
        Pageable pageable   = PageRequest.of(page, size, Sort.by("rating").descending());
        String safeLocation = (location == null)  ? ""                          : location;
        BigDecimal safePrice = (price == null)    ? new BigDecimal("999999999") : price;

        Page<Hotel> hotels;

        // ✅ Facility filter provided — use JPQL JOIN query
        if (facilities != null && !facilities.isBlank()) {
            hotels = hotelRepository.findByActiveTrueAndLocationAndPriceAndFacility(
                    safeLocation, safePrice, facilities.trim(), pageable);
        } else {
            // No facility filter — use original derived query
            hotels = hotelRepository
                    .findByActiveTrueAndLocationContainingIgnoreCaseAndMinPriceLessThanEqual(
                            safeLocation, safePrice, pageable);
        }

        return hotels.map(hotel -> {
            List<HotelImage> images = hotelImageRepository.findByHotel(hotel);
            String thumbnail = images.isEmpty() ? null : images.get(0).getImageUrl();
            return new HotelSearchResponse(
                    hotel.getId(), hotel.getName(), hotel.getLocation(),
                    thumbnail, hotel.getRating(), hotel.getMinPrice());
        });
    }

    // ─── Hotel detail ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public HotelDetailResponse getHotelDetails(Long hotelId) {
        Hotel hotel = findHotel(hotelId);

        List<HotelImageResponse> images = hotelImageRepository.findByHotel(hotel).stream()
                .map(img -> new HotelImageResponse(img.getId(), img.getImageUrl()))
                .toList();

        List<RoomResponse> rooms = roomRepository.findByHotelId(hotelId).stream()
                .map(room -> new RoomResponse(
                        room.getId(), room.getRoomType(), room.getRoomCount(),
                        room.getAvailableCount(), room.getPricePerNight(), room.isAvailable()))
                .toList();

        return new HotelDetailResponse(
                hotel.getId(), hotel.getName(), hotel.getLocation(),
                hotel.getDescription(), hotel.getFacilities(),
                hotel.getRating(), images, rooms);
    }

    // ─── Add room ─────────────────────────────────────────────────────────────
    public RoomResponse addRoom(Long hotelId, CreateRoomRequest request, String requesterEmail) {
        User owner = findUser(requesterEmail);
        Hotel hotel = findHotel(hotelId);

        if (!hotel.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the hotel owner can add rooms");
        }

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomType(request.getRoomType());
        room.setRoomCount(request.getRoomCount());
        room.setAvailableCount(request.getRoomCount());
        room.setPricePerNight(request.getPricePerNight());
        room.setAvailable(true);
        Room saved = roomRepository.save(room);

        if (hotel.getMinPrice().compareTo(BigDecimal.ZERO) == 0
                || request.getPricePerNight().compareTo(hotel.getMinPrice()) < 0) {
            hotel.setMinPrice(request.getPricePerNight());
            hotelRepository.save(hotel);
        }

        return new RoomResponse(saved.getId(), saved.getRoomType(), saved.getRoomCount(),
                saved.getAvailableCount(), saved.getPricePerNight(), saved.isAvailable());
    }

    // ─── Delete room ──────────────────────────────────────────────────────────
    public String deleteRoom(Long hotelId, Long roomId, String requesterEmail) {
        User owner = findUser(requesterEmail);
        Hotel hotel = findHotel(hotelId);

        if (!hotel.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only the hotel owner can delete rooms");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new BadRequestException("Room does not belong to this hotel");
        }

        boolean hasActiveReservations = reservationRepository.findByHotelOrderByCreatedAtDesc(hotel)
                .stream()
                .anyMatch(r -> r.getRoom().getId().equals(roomId)
                        && (r.getStatus().name().equals("PENDING")
                        || r.getStatus().name().equals("CONFIRMED")));

        if (hasActiveReservations) {
            throw new BadRequestException("Cannot delete room with active reservations");
        }

        roomRepository.delete(room);

        List<Room> remainingRooms = roomRepository.findByHotelId(hotelId);
        BigDecimal newMin = remainingRooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        hotel.setMinPrice(newMin);
        hotelRepository.save(hotel);

        return "Room deleted successfully";
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Hotel findHotel(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }
}