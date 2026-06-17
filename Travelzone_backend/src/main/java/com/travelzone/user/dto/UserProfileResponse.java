package com.travelzone.user.dto;

public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profilePicture;
    private String bio;
    private String role;

    public UserProfileResponse(Long id, String name, String email, String phone,
                               String profilePicture, String bio, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public String getRole() {
        return role;
    }
}
