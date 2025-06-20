package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с информацией о счете")
public class AccountResponse {
    @Schema(description = "Идентификатор пользователя")
    private String userId;

    @Schema(description = "Баланс счета")
    private int balance;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
} 