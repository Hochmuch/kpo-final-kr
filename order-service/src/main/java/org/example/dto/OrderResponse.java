package org.example.dto;

import org.example.model.OrderStatus;

public class OrderResponse {
    private String id;
    private String userId;
    private String productName;
    private int amount;
    private String description;
    private OrderStatus status;

    public OrderResponse() {
    }

    public OrderResponse(String id, String userId, String productName, int amount, String description, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.productName = productName;
        this.amount = amount;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
} 