package org.example.controller;

import org.example.model.Account;
import org.example.service.AccountService;
import org.example.dto.AccountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        if (userId == null || userId.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "userId is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Account account = accountService.createAccount(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", account.getUserId());
            response.put("balance", account.getBalance());
            response.put("status", account.getStatus().name());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<?> depositMoney(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        Object amountObj = request.get("amount");
        if (amountObj == null) {
            return ResponseEntity.badRequest().body("amount is required");
        }
        int amount;
        try {
            if (amountObj instanceof Integer) {
                amount = (Integer) amountObj;
            } else if (amountObj instanceof String) {
                amount = Integer.parseInt((String) amountObj);
            } else {
                return ResponseEntity.badRequest().body("Invalid amount format");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format");
        }
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("amount must be positive");
        }
        try {
            Account account = accountService.deposit(userId, amount);
            AccountResponse response = new AccountResponse();
            response.setUserId(account.getUserId());
            response.setBalance(account.getBalance());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable String userId) {
        return accountService.getAccount(userId)
                .map(account -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", account.getUserId());
                    response.put("balance", account.getBalance());
                    response.put("status", account.getStatus().name());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 