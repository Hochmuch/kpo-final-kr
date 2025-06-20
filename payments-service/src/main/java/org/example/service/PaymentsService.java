package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.dto.OrderDto;
import org.example.dto.PaymentProcessedEvent;
import org.example.model.Payment;
import org.example.model.PaymentStatus;
import org.example.model.OutboxEvent;
import org.example.repository.PaymentRepository;
import org.example.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentsService {
    private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);


    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final UserBalanceService userBalanceService;
    private final ObjectMapper objectMapper;

    public PaymentsService(PaymentRepository paymentRepository,
                           OutboxRepository outboxRepository,
                           UserBalanceService userBalanceService,
                           ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.userBalanceService = userBalanceService;
        this.objectMapper = objectMapper;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Transactional
    @KafkaListener(topics = "orders-test", groupId = "payments-service-group-2")
    public void processOrder(String orderJson) {
        log.info("LOOK HERE");
        try {
            log.info("Received order for payment processing: {}", orderJson);

            OrderDto order = objectMapper.readValue(orderJson, OrderDto.class);

            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrderId(order.getId());
            payment.setUserId(order.getUserId());
            payment.setAmount(order.getAmount());
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);
            
            log.info("Created payment with id: {} for order: {}", payment.getId(), order.getId());

            processPayment(payment);
            
        } catch (Exception e) {
            log.error("Failed to process payment for order: {}", orderJson, e);
            throw new RuntimeException("Failed to process payment", e);
        }
    }


    @Transactional
    public void processPayment(Payment payment) {
        log.info("processPayment called for paymentId={}, userId={}, amount={}", payment.getId(), payment.getUserId(), payment.getAmount());
        try {
            if (!userBalanceService.hasAccount(payment.getUserId())) {
                String errorMessage = "User has no account";
                log.error("User {} has no account", payment.getUserId());
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(errorMessage);
                paymentRepository.save(payment);
                createOutboxEvent(payment, false, errorMessage);
                return;
            }

            if (!userBalanceService.hasEnoughMoney(payment.getUserId(), payment.getAmount())) {
                String errorMessage = "Insufficient funds";
                log.error("User {} has insufficient funds", payment.getUserId());
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(errorMessage);
                paymentRepository.save(payment);
                createOutboxEvent(payment, false, errorMessage);
                return;
            }

            try {
                userBalanceService.withdrawMoney(payment.getUserId(), payment.getAmount());
            } catch (Exception e) {
                String errorMessage = "Failed to withdraw money: " + e.getMessage();
                log.error("Failed to withdraw money for payment: {}", payment.getId(), e);
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(errorMessage);
                paymentRepository.save(payment);
                createOutboxEvent(payment, false, errorMessage);
                return;
            }

            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            createOutboxEvent(payment, true, null);

            log.info("Successfully processed payment: {}", payment.getId());

        } catch (Exception e) {
            String errorMessage = "Unexpected error: " + e.getMessage();
            log.error("Failed to process payment: {}", payment.getId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(errorMessage);
            paymentRepository.save(payment);
            createOutboxEvent(payment, false, errorMessage);
        }
    }

    private void createOutboxEvent(Payment payment, boolean success, String errorMessage) {
        log.info("createOutboxEvent called for paymentId={}, success={}, error={}", payment.getId(), success, errorMessage);
        try {
            PaymentProcessedEvent event = new PaymentProcessedEvent();
            event.setPaymentId(payment.getId());
            event.setOrderId(payment.getOrderId());
            event.setUserId(payment.getUserId());
            event.setAmount(payment.getAmount());
            event.setSuccess(success);
            event.setErrorMessage(errorMessage);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(payment.getId());
            outboxEvent.setEventType("PAYMENT_PROCESSED");
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to create outbox event for payment: {}", payment.getId(), e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
} 