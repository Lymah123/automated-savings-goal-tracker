package com.example.savings.controller;

import com.example.savings.model.SavingsGoal;
import com.example.savings.model.Transaction;
import com.example.savings.model.User;
import com.example.savings.service.SavingsGoalService;
import com.example.savings.service.TransactionService;
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
@RequestMapping("/api/goals")
public class SavingsGoalController {
    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getUserGoals(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<SavingsGoal> goals = savingsGoalService.getUserSavingsGoals(user);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goal = savingsGoalService.findById(id);
        if (goal.isEmpty() || !goal.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goal.get());
    }

    @PostMapping
    public ResponseEntity<?> createSavingsGoal(@Valid @RequestBody CreateGoalRequest request,
                                              Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        SavingsGoal goal = new SavingsGoal();
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());

        SavingsGoal savedGoal = savingsGoalService.createSavingsGoal(goal, user, request.getDestinationAccountId());

        return ResponseEntity.ok(savedGoal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSavingsGoal(@PathVariable Long id,
                                              @Valid @RequestBody UpdateGoalRequest request,
                                              Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(id);
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        SavingsGoal goal = goalOpt.get();

        if (request.getName() != null) {
            goal.setName(request.getName());
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updatedGoal = savingsGoalService.updateSavingsGoal(goal);

        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSavingsGoal(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goal = savingsGoalService.findById(id);
        if (goal.isEmpty() || !goal.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        savingsGoalService.deleteSavingsGoal(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Goal deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getGoalProgress(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(id);
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        SavingsGoal goal = goalOpt.get();
        double progress = savingsGoalService.calculateProgress(goal);

        Map<String, Object> response = new HashMap<>();
        response.put("goalId", id);
        response.put("name", goal.getName());
        response.put("targetAmount", goal.getTargetAmount());
        response.put("currentAmount", goal.getCurrentAmount());
        response.put("progressPercentage", progress);
        response.put("estimatedCompletionDate", savingsGoalService.estimateCompletionDate(goal));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getGoalTransactions(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(id);
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        List<Transaction> transactions = transactionService.getGoalTransactions(goalOpt.get());

        return ResponseEntity.ok(transactions);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Request classes
    public static class CreateGoalRequest {
        private String name;
        private Double targetAmount;
        private java.time.LocalDate targetDate;
        private Long destinationAccountId;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getTargetAmount() { return targetAmount; }
        public void setTargetAmount(Double targetAmount) { this.targetAmount = targetAmount; }
        public java.time.LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(java.time.LocalDate targetDate) { this.targetDate = targetDate; }
        public Long getDestinationAccountId() { return destinationAccountId; }
        public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }
    }

    public static class UpdateGoalRequest {
        private String name;
        private Double targetAmount;
        private java.time.LocalDate targetDate;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getTargetAmount() { return targetAmount; }
        public void setTargetAmount(Double targetAmount) { this.targetAmount = targetAmount; }
        public java.time.LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(java.time.LocalDate targetDate) { this.targetDate = targetDate; }
    }
}
