package com.example.savings.service;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.repository.SavingsRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SavingsRuleService {
    @Autowired
    private SavingsRuleRepository savingsRuleRepository;

    @Autowired
    private BankAccountService bankAccountService;

    public SavingsRule createSavingsRule(SavingsRule rule, SavingsGoal goal, Long sourceAccountId) {
        // Set the savings goal
        rule.setSavingsGoal(goal);

        // Set the source account
        Optional<BankAccount> sourceAccount = bankAccountService.findById(sourceAccountId);
        if (sourceAccount.isEmpty()) {
            throw new RuntimeException("Source account not found");
        }

        rule.setSourceAccount(sourceAccount.get());

        // Set as active by default
        rule.setIsActive(true);

        // Save and return
        return savingsRuleRepository.save(rule);
    }

    public List<SavingsRule> getGoalSavingsRules(SavingsGoal goal) {
        return savingsRuleRepository.findBySavingsGoal(goal);
    }

    public List<SavingsRule> getAllActiveRules() {
        return savingsRuleRepository.findByIsActiveTrue();
    }

    public Optional<SavingsRule> findById(Long id) {
        return savingsRuleRepository.findById(id);
    }

    public SavingsRule updateSavingsRule(SavingsRule rule) {
        return savingsRuleRepository.save(rule);
    }

    public void deleteSavingsRule(Long id) {
        savingsRuleRepository.deleteById(id);
    }

    public void toggleRuleStatus(Long id) {
        Optional<SavingsRule> ruleOpt = savingsRuleRepository.findById(id);
        if (ruleOpt.isPresent()) {
            SavingsRule rule = ruleOpt.get();
            rule.setIsActive(!rule.getIsActive());
            savingsRuleRepository.save(rule);
        }
    }
}
