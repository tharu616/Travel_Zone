package com.travelzone.payment.entity;

import com.travelzone.common.enums.PaymentStatus;
import com.travelzone.common.enums.PaymentType;
import com.travelzone.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tourist_id", nullable = false)
    private User tourist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Column(nullable = false)
    private Long referenceId; // guideBookingId or reservationId

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 100)
    private String paymentMethod;

    @Column(length = 255)
    private String transactionNote;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public Payment() {}

    public Long getId() { return id; }
    public User getTourist() { return tourist; }
    public void setTourist(User tourist) { this.tourist = tourist; }
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionNote() { return transactionNote; }
    public void setTransactionNote(String transactionNote) { this.transactionNote = transactionNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}