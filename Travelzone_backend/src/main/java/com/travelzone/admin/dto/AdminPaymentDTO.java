package com.travelzone.admin.dto;

import java.time.LocalDateTime;

public class AdminPaymentDTO {
    private Long id;
    private String payerName;
    private String payerEmail;
    private String type;
    private Double amount;
    private String status;
    private String stripeId;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminPaymentDTO obj = new AdminPaymentDTO();
        public Builder id(Long v)          { obj.id = v; return this; }
        public Builder payerName(String v) { obj.payerName = v; return this; }
        public Builder payerEmail(String v){ obj.payerEmail = v; return this; }
        public Builder type(String v)      { obj.type = v; return this; }
        public Builder amount(Double v)    { obj.amount = v; return this; }
        public Builder status(String v)    { obj.status = v; return this; }
        public Builder stripeId(String v)  { obj.stripeId = v; return this; }
        public Builder createdAt(LocalDateTime v){ obj.createdAt = v; return this; }
        public AdminPaymentDTO build()     { return obj; }
    }

    public Long getId()          { return id; }
    public String getPayerName() { return payerName; }
    public String getPayerEmail(){ return payerEmail; }
    public String getType()      { return type; }
    public Double getAmount()    { return amount; }
    public String getStatus()    { return status; }
    public String getStripeId()  { return stripeId; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
