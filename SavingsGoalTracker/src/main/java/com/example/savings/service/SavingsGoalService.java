package com.example.savings.service;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.User;
import com.example.savings.repository.SavingsGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SavingsGoalService {
    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private BankAccountService bankAccountService;

    public SavingsGoal createSavingsGoal(SavingsGoal savingsGoal, User user, Long destinationAccountId) {
        // Set the user
        savingsGoal.setUser(user);

        // Set the destination account
        Optional<BankAccount> destinationAccount = bankAccountService.findById(destinationAccountId);
        if (destinationAccount.isEmpty()) {
            throw new RuntimeException("Destination account not found");
        }

        savingsGoal.setDestinationAccount(destinationAccount.get());

        // Set initial values
        savingsGoal.setCurrentAmount(0.0);
        savingsGoal.setStartDate(LocalDate.now());

        // Save and return
        return savingsGoalRepository.save(savingsGoal);
    }

    public List<SavingsGoal> getUserSavingsGoals(User user) {
        return savingsGoalRepository.findByUser(user);
    }

    public Optional<SavingsGoal> findById(Long id) {
        return savingsGoalRepository.findById(id);
    }

    public SavingsGoal updateSavingsGoal(SavingsGoal savingsGoal) {
        return savingsGoalRepository.save(savingsGoal);
    }

    public void deleteSavingsGoal(Long id) {
        savingsGoalRepository.deleteById(id);
    }

    public double calculateProgress(SavingsGoal goal) {
        return (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;
    }

    public LocalDate estimateCompletionDate(SavingsGoal goal) {
        // This is a simple estimation based on current progress
        if (goal.getCurrentAmount() <= 0) {
            return goal.getTargetDate();
        }

        double dailyAverage = goal.getCurrentAmount() /
                              LocalDate.now().until(goal.getStartDate()).getDays();

        double daysRemaining = (goal.getTargetAmount() - goal.getCurrentAmount()) / dailyAverage;

        return LocalDate.now().plusDays((long) daysRemaining);
    }
}
