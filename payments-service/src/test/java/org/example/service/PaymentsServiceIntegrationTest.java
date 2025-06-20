package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.OrderDto;
import org.example.model.Payment;
import org.example.model.PaymentStatus;
import org.example.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"orders"})
public class PaymentsServiceIntegrationTest {

    @Autowired
    private PaymentsService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testProcessOrder_success() throws Exception {
        OrderDto order = new OrderDto();
        order.setId("order-1");
        order.setUserId("user-1");
        order.setAmount(100);
        order.setDescription("Test order");
        String orderJson = objectMapper.writeValueAsString(order);

        paymentService.processOrder(orderJson);

        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals("order-1"))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(payment);
        Assertions.assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        Assertions.assertEquals("user-1", payment.getUserId());
        Assertions.assertEquals(100, payment.getAmount());
    }
} 