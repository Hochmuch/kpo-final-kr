package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.OutboxEvent;
import org.example.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private static final String PAYMENT_RESULTS_TOPIC = "payment-results";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository outboxRepository,
                        KafkaTemplate<String, String> kafkaTemplate,
                        ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> unprocessedEvents = outboxRepository.findUnprocessed();
        
        for (OutboxEvent event : unprocessedEvents) {
            try {
                kafkaTemplate.send(PAYMENT_RESULTS_TOPIC, event.getAggregateId(), event.getPayload());

                event.setProcessed(true);
                outboxRepository.save(event);
                
                log.info("Successfully processed outbox event: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
            }
        }
    }
} 