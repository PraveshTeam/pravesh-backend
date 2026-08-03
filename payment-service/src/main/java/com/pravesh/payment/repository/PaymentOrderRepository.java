package com.pravesh.payment.repository;

import com.pravesh.payment.entity.PaymentOrder;
import com.pravesh.payment.entity.PaymentPurpose;
import com.pravesh.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);

    List<PaymentOrder> findByResidentIdOrderByCreatedAtDesc(Long residentId);

    List<PaymentOrder> findByPurposeAndStatusOrderByCreatedAtDesc(PaymentPurpose purpose, PaymentStatus status);

    // Scoped to the admin's own society -- this is what fixes the cross-society
    // data leak (an admin from one society was able to see every society's payments).
    List<PaymentOrder> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
}
