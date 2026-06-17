package com.travelzone.user.controller;

import com.travelzone.common.dto.ApiResponse;
import com.travelzone.user.dto.UpdateProfileRequest;
import com.travelzone.user.dto.UserProfileResponse;
import com.travelzone.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId,
                                                          Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(userId, authentication));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateProfile(@PathVariable Long userId,
                                                     @Valid @RequestBody UpdateProfileRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.ok(userService.updateProfile(userId, request, authentication));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteAccount(@PathVariable Long userId,
                                                     Authentication authentication) {
        return ResponseEntity.ok(userService.deleteAccount(userId, authentication));
    }
}
