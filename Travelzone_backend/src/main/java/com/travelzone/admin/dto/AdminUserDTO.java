package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminUserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String profilePicture;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminUserDTO obj = new AdminUserDTO();
        public Builder id(Long v)             { obj.id = v; return this; }
        public Builder name(String v)         { obj.name = v; return this; }
        public Builder email(String v)        { obj.email = v; return this; }
        public Builder phone(String v)        { obj.phone = v; return this; }
        public Builder role(String v)         { obj.role = v; return this; }
        public Builder profilePicture(String v){ obj.profilePicture = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminUserDTO build()           { return obj; }
    }

    public Long getId()             { return id; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getRole()         { return role; }
    public String getProfilePicture(){ return profilePicture; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
