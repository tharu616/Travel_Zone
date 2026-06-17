package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminReviewDTO {
    private Long id;
    private String reviewerName;
    private String reviewerEmail;
    private String targetName;
    private String targetType;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminReviewDTO obj = new AdminReviewDTO();
        public Builder id(Long v)             { obj.id = v; return this; }
        public Builder reviewerName(String v) { obj.reviewerName = v; return this; }
        public Builder reviewerEmail(String v){ obj.reviewerEmail = v; return this; }
        public Builder targetName(String v)   { obj.targetName = v; return this; }
        public Builder targetType(String v)   { obj.targetType = v; return this; }
        public Builder rating(Integer v)      { obj.rating = v; return this; }
        public Builder comment(String v)      { obj.comment = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminReviewDTO build()         { return obj; }
    }

    public Long getId()             { return id; }
    public String getReviewerName() { return reviewerName; }
    public String getReviewerEmail(){ return reviewerEmail; }
    public String getTargetName()   { return targetName; }
    public String getTargetType()   { return targetType; }
    public Integer getRating()      { return rating; }
    public String getComment()      { return comment; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
