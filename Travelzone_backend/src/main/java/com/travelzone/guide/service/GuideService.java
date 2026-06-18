package com.travelzone.guide.service;

import com.travelzone.common.enums.Role;
import com.travelzone.exception.BadRequestException;
import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.exception.UnauthorizedException;
import com.travelzone.guide.dto.CreateGuideProfileRequest;
import com.travelzone.guide.dto.GuideProfileResponse;
import com.travelzone.guide.dto.GuideSearchResponse;
import com.travelzone.guide.dto.UpdateGuideProfileRequest;
import com.travelzone.guide.entity.GuideAvailability;
import com.travelzone.guide.entity.GuideProfile;
import com.travelzone.guide.repository.GuideAvailabilityRepository;
import com.travelzone.guide.repository.GuideProfileRepository;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class GuideService {

    private final GuideProfileRepository guideProfileRepository;
    private final GuideAvailabilityRepository guideAvailabilityRepository;
    private final UserRepository userRepository;

    public GuideService(GuideProfileRepository guideProfileRepository,
                        GuideAvailabilityRepository guideAvailabilityRepository,
                        UserRepository userRepository) {
        this.guideProfileRepository = guideProfileRepository;
        this.guideAvailabilityRepository = guideAvailabilityRepository;
        this.userRepository = userRepository;
    }

    // ─── Create ───────────────────────────────────────────────────────────────
    public GuideProfileResponse createGuideProfile(CreateGuideProfileRequest request, String requesterEmail) {
        User user = findUser(requesterEmail);

        if (user.getRole() != Role.GUIDE) {
            throw new UnauthorizedException("Only GUIDE users can create guide profiles");
        }
        if (guideProfileRepository.existsByUser(user)) {
            throw new BadRequestException("Guide profile already exists");
        }

        GuideProfile profile = new GuideProfile();
        profile.setUser(user);
        profile.setExperienceYears(request.getExperienceYears());
        profile.setLanguages(request.getLanguages());
        profile.setPricePerDay(request.getPricePerDay());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setProfilePhoto(request.getProfilePhoto());

        GuideProfile saved = guideProfileRepository.save(profile);

        // Auto-generate 30 availability dates
        for (int i = 1; i <= 30; i++) {
            GuideAvailability availability = new GuideAvailability();
            availability.setGuideProfile(saved);
            availability.setAvailableDate(LocalDate.now().plusDays(i));
            availability.setAvailable(true);
            guideAvailabilityRepository.save(availability);
        }

        return mapToProfileResponse(saved);
    }

    // ─── Get own profile ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public GuideProfileResponse getMyGuideProfile(String requesterEmail) {
        User user = findUser(requesterEmail);
        GuideProfile profile = guideProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Guide profile not found"));
        return mapToProfileResponse(profile);
    }

    // ─── Get any guide profile by ID (for tourists) ───────────────────────────
    @Transactional(readOnly = true)
    public GuideProfileResponse getGuideProfile(Long guideId) {
        GuideProfile guide = guideProfileRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));
        return mapToProfileResponse(guide);
    }

    // ─── Search guides ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<GuideSearchResponse> searchGuides(String location, String language,
                                                   Double rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        String safeLocation = location == null ? "" : location;
        Double safeRating = rating == null ? 0.0 : rating;

        Page<GuideProfile> result;
        if (language != null && !language.isBlank()) {
            result = guideProfileRepository
                    .findByLocationContainingIgnoreCaseAndLanguagesContainingAndRatingGreaterThanEqual(
                            safeLocation, language, safeRating, pageable);
        } else {
            result = guideProfileRepository
                    .findByLocationContainingIgnoreCaseAndRatingGreaterThanEqual(
                            safeLocation, safeRating, pageable);
        }

        return result.map(guide -> new GuideSearchResponse(
                guide.getId(),
                guide.getUser().getName(),
                guide.getProfilePhoto(),
                guide.getRating(),
                guide.getPricePerDay()
        ));
    }

    // ─── Update own profile ───────────────────────────────────────────────────
    public GuideProfileResponse updateGuideProfile(UpdateGuideProfileRequest request, String requesterEmail) {
        User user = findUser(requesterEmail);
        GuideProfile profile = guideProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Guide profile not found"));

        profile.setExperienceYears(request.getExperienceYears());
        profile.setLanguages(request.getLanguages());
        profile.setPricePerDay(request.getPricePerDay());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());

        // Only update photo if a new one was provided
        if (request.getProfilePhoto() != null && !request.getProfilePhoto().isBlank()) {
            profile.setProfilePhoto(request.getProfilePhoto());
        }

        GuideProfile updated = guideProfileRepository.save(profile);
        return mapToProfileResponse(updated);
    }

    // ─── Delete own profile ───────────────────────────────────────────────────
    public void deleteGuideProfile(String requesterEmail) {
        User user = findUser(requesterEmail);
        GuideProfile profile = guideProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Guide profile not found"));

        // Delete availability records first (FK constraint)
        List<GuideAvailability> availabilities = guideAvailabilityRepository.findAll().stream()
                .filter(a -> a.getGuideProfile().getId().equals(profile.getId()))
                .toList();
        guideAvailabilityRepository.deleteAll(availabilities);

        guideProfileRepository.delete(profile);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private GuideProfileResponse mapToProfileResponse(GuideProfile guide) {
        List<LocalDate> availableDates = guideAvailabilityRepository.findAll().stream()
                .filter(a -> a.getGuideProfile().getId().equals(guide.getId()) && a.isAvailable())
                .map(GuideAvailability::getAvailableDate)
                .toList();

        return new GuideProfileResponse(
                guide.getId(),
                guide.getUser().getName(),
                guide.getExperienceYears(),
                guide.getLanguages(),
                guide.getPricePerDay(),
                guide.getLocation(),
                guide.getBio(),
                guide.getProfilePhoto(),
                guide.getRating(),
                availableDates
        );
    }
}
