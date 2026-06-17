package com.travelzone.admin.dto;

public class AdminStatsDTO {
    private long totalUsers;
    private long totalGuides;
    private long totalHotels;
    private long totalBookings;
    private long totalReservations;
    private long totalPayments;
    private double totalRevenue;
    private long pendingBookings;
    private long pendingReservations;
    private long totalReviews;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminStatsDTO obj = new AdminStatsDTO();
        public Builder totalUsers(long v)        { obj.totalUsers = v; return this; }
        public Builder totalGuides(long v)       { obj.totalGuides = v; return this; }
        public Builder totalHotels(long v)       { obj.totalHotels = v; return this; }
        public Builder totalBookings(long v)     { obj.totalBookings = v; return this; }
        public Builder totalReservations(long v) { obj.totalReservations = v; return this; }
        public Builder totalPayments(long v)     { obj.totalPayments = v; return this; }
        public Builder totalRevenue(double v)    { obj.totalRevenue = v; return this; }
        public Builder pendingBookings(long v)   { obj.pendingBookings = v; return this; }
        public Builder pendingReservations(long v){ obj.pendingReservations = v; return this; }
        public Builder totalReviews(long v)      { obj.totalReviews = v; return this; }
        public AdminStatsDTO build()             { return obj; }
    }

    public long getTotalUsers()        { return totalUsers; }
    public long getTotalGuides()       { return totalGuides; }
    public long getTotalHotels()       { return totalHotels; }
    public long getTotalBookings()     { return totalBookings; }
    public long getTotalReservations() { return totalReservations; }
    public long getTotalPayments()     { return totalPayments; }
    public double getTotalRevenue()    { return totalRevenue; }
    public long getPendingBookings()   { return pendingBookings; }
    public long getPendingReservations(){ return pendingReservations; }
    public long getTotalReviews()      { return totalReviews; }
}
