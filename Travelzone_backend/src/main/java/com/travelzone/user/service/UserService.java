package com.travelzone.user.service;

import com.travelzone.common.dto.ApiResponse;
import com.travelzone.common.enums.Role;
import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.exception.UnauthorizedException;
import com.travelzone.user.dto.UpdateProfileRequest;
import com.travelzone.user.dto.UserProfileResponse;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(Long userId, Authentication authentication) {
        User currentUser = userRepository.findByEmailAndDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        User targetUser = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own profile");
        }

        return new UserProfileResponse(
                targetUser.getId(),
                targetUser.getName(),
                targetUser.getEmail(),
                targetUser.getPhone(),
                targetUser.getProfilePicture(),
                targetUser.getBio(),
                targetUser.getRole().name()
        );
    }

    public ApiResponse updateProfile(Long userId, UpdateProfileRequest request, Authentication authentication) {
        User currentUser = userRepository.findByEmailAndDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        User targetUser = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        targetUser.setName(request.getName());
        targetUser.setPhone(request.getPhone());
        targetUser.setProfilePicture(request.getProfilePicture());
        targetUser.setBio(request.getBio());

        userRepository.save(targetUser);
        return new ApiResponse(true, "Profile updated successfully");
    }

    public ApiResponse deleteAccount(Long userId, Authentication authentication) {
        User currentUser = userRepository.findByEmailAndDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        User targetUser = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own account");
        }

        targetUser.setDeleted(true);
        userRepository.save(targetUser);

        return new ApiResponse(true, "Account deleted successfully");
    }
}
