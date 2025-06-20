package org.example.service;

import org.example.model.Account;
import org.example.model.AccountStatus;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String userId) {
        if (accountRepository.existsById(userId)) {
            throw new IllegalArgumentException("Account for user " + userId + " already exists");
        }
        
        Account account = new Account(userId);
        return accountRepository.save(account);
    }

    public Optional<Account> getAccount(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account deposit(String userId, int amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userId));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active for user: " + userId);
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        account.deposit(amount);
        return accountRepository.save(account);
    }

    public boolean withdraw(String userId, int amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userId));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active for user: " + userId);
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        boolean success = account.withdraw(amount);
        if (success) {
            accountRepository.save(account);
        }
        return success;
    }

    public boolean hasEnoughBalance(String userId, int amount) {
        return accountRepository.findByUserId(userId)
                .map(account -> account.getBalance() >= amount)
                .orElse(false);
    }
} 