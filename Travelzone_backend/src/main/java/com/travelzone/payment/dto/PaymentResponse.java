package com.travelzone.payment.dto;

import com.travelzone.common.enums.PaymentStatus;
import com.travelzone.common.enums.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long touristId;
    private String touristName;
    private PaymentType paymentType;
    private Long referenceId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionNote;
    private LocalDateTime createdAt;

    public PaymentResponse(Long id, Long touristId, String touristName,
                           PaymentType paymentType, Long referenceId,
                           BigDecimal amount, PaymentStatus status,
                           String paymentMethod, String transactionNote,
                           LocalDateTime createdAt) {
        this.id = id; this.touristId = touristId; this.touristName = touristName;
        this.paymentType = paymentType; this.referenceId = referenceId;
        this.amount = amount; this.status = status;
        this.paymentMethod = paymentMethod; this.transactionNote = transactionNote;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTouristId() { return touristId; }
    public String getTouristName() { return touristName; }
    public PaymentType getPaymentType() { return paymentType; }
    public Long getReferenceId() { return referenceId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionNote() { return transactionNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}