package org.example.dto;

public class AccountResponse {
    private String userId;
    private int balance;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
} 