package org.example.repository;

import org.example.model.Payment;
import org.example.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByStatusAndRetryCountLessThanAndLastRetryTimeBefore(
        PaymentStatus status, int maxRetries, LocalDateTime retryTime);
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByUserId(String userId);
} 