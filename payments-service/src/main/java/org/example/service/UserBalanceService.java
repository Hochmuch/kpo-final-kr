package org.example.service;

import org.example.model.Account;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserBalanceService {
    private final AccountService accountService;

    public UserBalanceService(AccountService accountService) {
        this.accountService = accountService;
    }
    
    public boolean hasAccount(String userId) {
        return accountService.getAccount(userId).isPresent();
    }
    
    public boolean hasEnoughMoney(String userId, int amount) {
        return accountService.hasEnoughBalance(userId, amount);
    }
    
    public boolean withdrawMoney(String userId, int amount) {
        return accountService.withdraw(userId, amount);
    }

    public Optional<Account> getAccount(String userId) {
        return accountService.getAccount(userId);
    }

    public Account depositMoney(String userId, int amount) {
        return accountService.deposit(userId, amount);
    }
} 