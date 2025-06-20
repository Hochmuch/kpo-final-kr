package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.dto.CreateOrderRequest;
import org.example.dto.PaymentProcessedEvent;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.OutboxEvent;
import org.example.model.Product;
import org.example.repository.OrderRepository;
import org.example.repository.OutboxRepository;
import org.example.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDERS_TOPIC = "orders-test";

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository,
                        OutboxRepository outboxRepository,
                        ProductRepository productRepository,
                        KafkaTemplate<String, String> kafkaTemplate,
                        ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Product product = productRepository.findByName(request.getProductName());
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + request.getProductName());
        }
        
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(request.getUserId());
        order.setProduct(product);
        order.setDescription(request.getDescription());
        order.setStatus(OrderStatus.NEW);
        
        Order savedOrder = orderRepository.save(order);

        createOutboxEvent(savedOrder);
        
        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Optional<Order> cancelOrder(String id) {
        return orderRepository.findById(id)
                .map(order -> {
                    if (order.getStatus() == OrderStatus.NEW) {
                        order.setStatus(OrderStatus.CANCELLED);
                        return orderRepository.save(order);
                    }
                    return order;
                });
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutbox() {
        log.info("Starting outbox processing");
        outboxRepository.findUnprocessed().forEach(event -> {
            try {
                log.info("Processing outbox event: {}", event.getId());
                kafkaTemplate.send(ORDERS_TOPIC, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent event {} to Kafka", event.getId());
                            event.setProcessed(true);
                            outboxRepository.save(event);
                        } else {
                            log.error("Failed to send event {} to Kafka", event.getId(), ex);
                        }
                    });
            } catch (Exception e) {
                log.error("Error processing outbox event: {}", event.getId(), e);
            }
        });
        log.info("Finished outbox processing");
    }

    @KafkaListener(topics = "payment-results", groupId = "orders")
    @Transactional
    public void processPaymentResult(String paymentResultJson) {
        try {
            log.info("Received payment result: {}", paymentResultJson);

            PaymentProcessedEvent paymentResult = objectMapper.readValue(paymentResultJson, PaymentProcessedEvent.class);

            Order order = orderRepository.findById(paymentResult.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + paymentResult.getOrderId()));

            if (paymentResult.isSuccess()) {
                order.setStatus(OrderStatus.FINISHED);
                log.info("Order {} has been paid successfully", order.getId());
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                log.error("Payment failed for order {}: {}", order.getId(), paymentResult.getErrorMessage());
            }

            orderRepository.save(order);
            
        } catch (Exception e) {
            log.error("Failed to process payment result: {}", paymentResultJson, e);
            throw new RuntimeException("Failed to process payment result", e);
        }
    }

    @KafkaListener(topics = "orders-test", groupId = "debug-group")
    public void debugKafka(String msg) {
        log.info("DEBUG KAFKA: received message: {}", msg);
    }

    private void createOutboxEvent(Order order) {
        log.info("createOutboxEvent called for orderId={}", order.getId());
        try {
            org.example.dto.OrderDto orderDto = new org.example.dto.OrderDto();
            orderDto.setId(order.getId());
            orderDto.setUserId(order.getUserId());
            orderDto.setAmount(order.getAmount());
            orderDto.setDescription(order.getDescription());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(order.getId());
            outboxEvent.setEventType("OrderCreated");
            outboxEvent.setPayload(objectMapper.writeValueAsString(orderDto));
            outboxRepository.save(outboxEvent);
            log.info("Created outbox event for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to create outbox event for order: {}", order.getId(), e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
