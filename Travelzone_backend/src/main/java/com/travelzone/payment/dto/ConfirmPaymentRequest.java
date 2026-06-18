package com.travelzone.payment.dto;

import com.travelzone.common.enums.PaymentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ConfirmPaymentRequest {

    @NotBlank
    private String paymentIntentId;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    private Long referenceId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String transactionNote;

    public ConfirmPaymentRequest() {}
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String id) { this.paymentIntentId = id; }
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTransactionNote() { return transactionNote; }
    public void setTransactionNote(String note) { this.transactionNote = note; }
}