package com.example.savings.service;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.Transaction;
import com.example.savings.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankApiService bankApiService;

    @Autowired
    private SavingsGoalService savingsGoalService;

    public Transaction createTransaction(SavingsGoal goal, BankAccount sourceAccount,
                                        SavingsRule rule, Double amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setSourceAccount(sourceAccount);
        transaction.setSavingsRule(rule);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // Save the transaction first
        transaction = transactionRepository.save(transaction);

        // Attempt to transfer funds
        boolean transferSuccess = bankApiService.transferFunds(
            sourceAccount,
            goal.getDestinationAccount(),
            amount
        );

        if (transferSuccess) {
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

            // Update the goal's current amount
            goal.setCurrentAmount(goal.getCurrentAmount() + amount);
            savingsGoalService.updateSavingsGoal(goal);
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
        }

        // Update the transaction status
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getGoalTransactions(SavingsGoal goal) {
        return transactionRepository.findBySavingsGoal(goal);
    }

    public List<Transaction> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTimestampBetween(start, end);
    }
}
