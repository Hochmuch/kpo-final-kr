package org.example.dto;

import java.util.Objects;

public class OrderResponse {
    private String id;
    private String userId;
    private String productName;
    private int amount;
    private String description;
    private String status;

    public OrderResponse() {
    }

    public OrderResponse(String id, String userId, String productName, int amount, String description, String status) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderResponse that = (OrderResponse) o;
        return amount == that.amount && Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(productName, that.productName) && Objects.equals(description, that.description) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, productName, amount, description, status);
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", productName='" + productName + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
} 