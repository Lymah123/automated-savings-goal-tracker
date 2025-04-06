package com.example.savings.controller;

import com.example.savings.model.BankAccount;
import com.example.savings.model.User;
import com.example.savings.service.BankAccountService;
import com.example.savings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {
    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<BankAccount>> getUserAccounts(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<BankAccount> accounts = bankAccountService.getUserAccounts(user);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<BankAccount> account = bankAccountService.findById(id);
        if (account.isEmpty() || !account.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(account.get());
    }

    @PostMapping
    public ResponseEntity<?> addBankAccount(@Valid @RequestBody BankAccount bankAccount,
                                           Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        BankAccount savedAccount = bankAccountService.addBankAccount(bankAccount, user);
        return ResponseEntity.ok(savedAccount);
    }

    @PostMapping("/link")
    public ResponseEntity<?> linkBankAccount(@Valid @RequestBody LinkAccountRequest request,
                                            Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        BankAccount linkedAccount = bankAccountService.linkBankAccount(
            request.getPublicToken(),
            request.getAccountId(),
            user
        );

        return ResponseEntity.ok(linkedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBankAccount(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<BankAccount> account = bankAccountService.findById(id);
        if (account.isEmpty() || !account.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        bankAccountService.deleteBankAccount(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Account deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> refreshAccountBalance(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<BankAccount> account = bankAccountService.findById(id);
        if (account.isEmpty() || !account.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        BankAccount refreshedAccount = bankAccountService.refreshAccountBalance(id);
        return ResponseEntity.ok(refreshedAccount);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Request class for linking accounts
    public static class LinkAccountRequest {
        private String publicToken;
        private String accountId;

        // Getters and setters
        public String getPublicToken() { return publicToken; }
        public void setPublicToken(String publicToken) { this.publicToken = publicToken; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
    }
}
