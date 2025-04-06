package com.example.savings.controller;

import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.User;
import com.example.savings.service.SavingsGoalService;
import com.example.savings.service.SavingsRuleService;
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
@RequestMapping("/api/rules")
public class SavingsRuleController {
    @Autowired
    private SavingsRuleService savingsRuleService;

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private UserService userService;

    @GetMapping("/goal/{goalId}")
    public ResponseEntity<?> getGoalRules(@PathVariable Long goalId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(goalId);
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        List<SavingsRule> rules = savingsRuleService.getGoalSavingsRules(goalOpt.get());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRuleById(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsRule> ruleOpt = savingsRuleService.findById(id);
        if (ruleOpt.isEmpty() || !ruleOpt.get().getSavingsGoal().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ruleOpt.get());
    }

    @PostMapping
    public ResponseEntity<?> createSavingsRule(@Valid @RequestBody CreateRuleRequest request,
                                              Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsGoal> goalOpt = savingsGoalService.findById(request.getSavingsGoalId());
        if (goalOpt.isEmpty() || !goalOpt.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        SavingsRule rule = new SavingsRule();
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setRuleType(request.getRuleType());
        rule.setRuleCondition(request.getRuleCondition());
        rule.setAmount(request.getAmount());
        rule.setIsActive(true);

        SavingsRule savedRule = savingsRuleService.createSavingsRule(
            rule,
            goalOpt.get(),
            request.getSourceAccountId()
        );

        return ResponseEntity.ok(savedRule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSavingsRule(@PathVariable Long id,
                                              @Valid @RequestBody UpdateRuleRequest request,
                                              Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsRule> ruleOpt = savingsRuleService.findById(id);
        if (ruleOpt.isEmpty() || !ruleOpt.get().getSavingsGoal().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        SavingsRule rule = ruleOpt.get();

        if (request.getName() != null) {
            rule.setName(request.getName());
        }

        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }

        if (request.getRuleCondition() != null) {
            rule.setRuleCondition(request.getRuleCondition());
        }

        if (request.getAmount() != null) {
            rule.setAmount(request.getAmount());
        }

        SavingsRule updatedRule = savingsRuleService.updateSavingsRule(rule);

        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSavingsRule(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsRule> ruleOpt = savingsRuleService.findById(id);
        if (ruleOpt.isEmpty() || !ruleOpt.get().getSavingsGoal().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        savingsRuleService.deleteSavingsRule(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Rule deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleRuleStatus(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        Optional<SavingsRule> ruleOpt = savingsRuleService.findById(id);
        if (ruleOpt.isEmpty() || !ruleOpt.get().getSavingsGoal().getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        savingsRuleService.toggleRuleStatus(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Rule status toggled successfully");
        response.put("isActive", !ruleOpt.get().getIsActive()); // The new status after toggle

        return ResponseEntity.ok(response);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Request classes
    public static class CreateRuleRequest {
        private String name;
        private String description;
        private SavingsRule.RuleType ruleType;
        private String ruleCondition;
        private Double amount;
        private Long savingsGoalId;
        private Long sourceAccountId;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public SavingsRule.RuleType getRuleType() { return ruleType; }
        public void setRuleType(SavingsRule.RuleType ruleType) { this.ruleType = ruleType; }
        public String getRuleCondition() { return ruleCondition; }
        public void setRuleCondition(String ruleCondition) { this.ruleCondition = ruleCondition; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Long getSavingsGoalId() { return savingsGoalId; }
        public void setSavingsGoalId(Long savingsGoalId) { this.savingsGoalId = savingsGoalId; }
        public Long getSourceAccountId() { return sourceAccountId; }
        public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }
    }

    public static class UpdateRuleRequest {
        private String name;
        private String description;
        private String ruleCondition;
        private Double amount;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRuleCondition() { return ruleCondition; }
        public void setRuleCondition(String ruleCondition) { this.ruleCondition = ruleCondition; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}
