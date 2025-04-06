package com.example.savings.controller;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.Transaction;
import com.example.savings.model.User;
import com.example.savings.service.BankAccountService;
import com.example.savings.service.SavingsGoalService;
import com.example.savings.service.SavingsRuleService;
import com.example.savings.service.TransactionService;
import com.example.savings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private SavingsRuleService savingsRuleService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;

    @GetMapping("/goal/{goalId}")
    public ResponseEntity<?> getGoalTransactions(@PathVariable Long goalId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(goalId);
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        List<Transaction> transactions = transactionService.getGoalTransactions(goalOpt.get());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<?> getTransactionsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {

        // In a real app, you would filter by user
        User user = getUserFromAuthentication(authentication);

        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(start, end);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/manual")
    public ResponseEntity<?> createManualTransaction(@Valid @RequestBody CreateTransactionRequest request,
                                                   Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        // Verify goal belongs to user
        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(request.getGoalId());
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        // Verify source account belongs to user
        Optional<BankAccount> accountOpt = bankAccountService.findById(request.getSourceAccountId());
        if (accountOpt.isEmpty() || !accountOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        // Create transaction (null rule indicates manual transaction)
        Transaction transaction = transactionService.createTransaction(
            goalOpt.get(),
            accountOpt.get(),
            null,
            request.getAmount(),
            request.getDescription()
        );

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/rule/{ruleId}")
    public ResponseEntity<?> triggerRuleTransaction(@PathVariable Long ruleId,
                                                  Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        // Verify rule and associated goal belong to user
        Optional<SavingsRule> ruleOpt = savingsRuleService.findById(ruleId);
        if (ruleOpt.isEmpty() || !ruleOpt.get().getSavingsGoal().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        SavingsRule rule = ruleOpt.get();

        // For fixed amount rules, we can trigger them directly
        if (rule.getRuleType() == SavingsRule.RuleType.FIXED_AMOUNT) {
            Transaction transaction = transactionService.createTransaction(
                rule.getSavingsGoal(),
                rule.getSourceAccount(),
                rule,
                rule.getAmount(),
                "Manual trigger: " + rule.getName()
            );

            return ResponseEntity.ok(transaction);
        } else {
            // For other rule types, we would need more complex logic
            // This is simplified for the example
            return ResponseEntity.badRequest().body(
                Map.of("error", "Manual triggering is only supported for fixed amount rules")
            );
        }
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Request class
    public static class CreateTransactionRequest {
        private Long goalId;
        private Long sourceAccountId;
        private Double amount;
        private String description;

        // Getters and setters
        public Long getGoalId() { return goalId; }
        public void setGoalId(Long goalId) { this.goalId = goalId; }
        public Long getSourceAccountId() { return sourceAccountId; }
        public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
