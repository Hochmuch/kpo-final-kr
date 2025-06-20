package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.AccountResponse;
import org.example.dto.CreateAccountRequest;
import org.example.dto.CreateOrderRequest;
import org.example.dto.DepositRequest;
import org.example.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@Tag(name = "API Gateway", description = "API для работы с заказами и счетами")
public class GatewayController {
    private final RestTemplate restTemplate;

    @Value("${services.order-service.url}")
    private String orderServiceUrl;
    
    @Value("${services.payment-service.url}")
    private String paymentServiceUrl;

    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/orders/create")
    @Operation(summary = "Создать новый заказ", description = "Создает новый заказ в системе")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {
        return restTemplate.postForEntity(orderServiceUrl + "/orders/create", request, OrderResponse.class);
    }


    @Operation(summary = "Получить список всех заказов", description = "Возвращает список всех заказов в системе")
    @GetMapping("/orders/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return restTemplate.exchange(
            orderServiceUrl + "/orders/all",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<OrderResponse>>() {}
        );
    }

    @Operation(summary = "Создать новый счет", description = "Создает новый счет для пользователя")
    @PostMapping("/accounts/create")
    public ResponseEntity<AccountResponse> createAccount(
            @Parameter(description = "Данные для создания счета") @RequestBody CreateAccountRequest request) {
        return restTemplate.postForEntity(paymentServiceUrl + "/accounts/create", request, AccountResponse.class);
    }

    @Operation(summary = "Пополнить счет", description = "Пополняет баланс счета пользователя")
    @PostMapping("/accounts/{userId}/deposit")
    public ResponseEntity<AccountResponse> depositMoney(
            @Parameter(description = "Идентификатор пользователя") @PathVariable String userId,
            @Parameter(description = "Данные для пополнения счета") @RequestBody DepositRequest request) {
        return restTemplate.postForEntity(paymentServiceUrl + "/accounts/" + userId + "/deposit", request, AccountResponse.class);
    }

    @Operation(summary = "Получить баланс счета", description = "Возвращает текущий баланс счета пользователя")
    @GetMapping("/accounts/{userId}")
    public ResponseEntity<AccountResponse> getAccountBalance(
            @Parameter(description = "Идентификатор пользователя") @PathVariable String userId) {
        return restTemplate.getForEntity(paymentServiceUrl + "/accounts/" + userId, AccountResponse.class);
    }

    @Operation(summary = "Получить статус заказа по id", description = "Возвращает информацию о заказе по его идентификатору")
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        return restTemplate.getForEntity(orderServiceUrl + "/orders/" + id, OrderResponse.class);
    }
} 