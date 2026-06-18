package com.travelzone.payment.dto;

import com.travelzone.common.enums.PaymentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "Reference ID is required")
    private Long referenceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String transactionNote;

    public CreatePaymentRequest() {}
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionNote() { return transactionNote; }
    public void setTransactionNote(String transactionNote) { this.transactionNote = transactionNote; }
}