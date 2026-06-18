package com.travelzone.guide.controller;

import com.travelzone.guide.dto.CreateGuideProfileRequest;
import com.travelzone.guide.dto.GuideProfileResponse;
import com.travelzone.guide.dto.GuideSearchResponse;
import com.travelzone.guide.dto.UpdateGuideProfileRequest;
import com.travelzone.guide.service.GuideService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    // ✅ Create guide profile (GUIDE only)
    @PostMapping
    public ResponseEntity<GuideProfileResponse> createGuideProfile(
            @Valid @RequestBody CreateGuideProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guideService.createGuideProfile(request, authentication.getName()));
    }

    // ✅ Search guides (any authenticated user — tourist, guide, hotel owner)
    @GetMapping
    public ResponseEntity<Page<GuideSearchResponse>> searchGuides(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Double rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(guideService.searchGuides(location, language, rating, page, size));
    }

    // ✅ Get guide's OWN profile (GUIDE only)
    @GetMapping("/me")
    public ResponseEntity<GuideProfileResponse> getMyGuideProfile(Authentication authentication) {
        return ResponseEntity.ok(guideService.getMyGuideProfile(authentication.getName()));
    }

    // ✅ Get any guide's profile by ID (tourist views this)
    @GetMapping("/{guideId}")
    public ResponseEntity<GuideProfileResponse> getGuideProfile(@PathVariable Long guideId) {
        return ResponseEntity.ok(guideService.getGuideProfile(guideId));
    }

    // ✅ Update own guide profile (GUIDE only)
    @PutMapping("/me")
    public ResponseEntity<GuideProfileResponse> updateMyGuideProfile(
            @Valid @RequestBody UpdateGuideProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(guideService.updateGuideProfile(request, authentication.getName()));
    }

    // ✅ Delete own guide profile (GUIDE only)
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyGuideProfile(Authentication authentication) {
        guideService.deleteGuideProfile(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
