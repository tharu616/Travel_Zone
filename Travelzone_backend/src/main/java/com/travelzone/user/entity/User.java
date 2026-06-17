package com.travelzone.user.entity;

import com.travelzone.common.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String bio;

    private String phone;

    // ✅ Fixed: LONGTEXT supports Base64 image strings
    @Column(name = "profile_picture", columnDefinition = "LONGTEXT")
    private String profilePicture;

    public User() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public boolean isDeleted() { return deleted; }
    public boolean isActive() { return active; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getProfilePicture() { return profilePicture; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public void setActive(boolean active) { this.active = active; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}
