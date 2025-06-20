package org.example.service;

import org.example.model.Payment;
import org.example.model.PaymentStatus;
import org.example.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RetryService {
    private static final Logger log = LoggerFactory.getLogger(RetryService.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MINUTES = 5;

    private final PaymentRepository paymentRepository;
    private final PaymentsService paymentService;

    public RetryService(PaymentRepository paymentRepository, PaymentsService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processRetries() {
        LocalDateTime retryTime = LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES);
        
        List<Payment> failedPayments = paymentRepository.findByStatusAndRetryCountLessThanAndLastRetryTimeBefore(
            PaymentStatus.FAILED, MAX_RETRIES, retryTime);
        
        for (Payment payment : failedPayments) {
            try {
                log.info("Retrying payment: {}", payment.getId());
                payment.setRetryCount(payment.getRetryCount() + 1);
                payment.setLastRetryTime(LocalDateTime.now());
                paymentRepository.save(payment);
                
                paymentService.processPayment(payment);
            } catch (Exception e) {
                log.error("Failed to retry payment: {}", payment.getId(), e);
            }
        }
    }
} 