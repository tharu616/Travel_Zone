package com.travelzone.payment.repository;

import com.travelzone.common.enums.PaymentType;
import com.travelzone.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    List<Payment> findByPaymentTypeAndReferenceIdInOrderByCreatedAtDesc(
            PaymentType type, List<Long> referenceIds);

    boolean existsByPaymentTypeAndReferenceId(PaymentType type, Long referenceId);
}