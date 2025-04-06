package com.example.savings.repository;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySavingsGoal(SavingsGoal savingsGoal);

    List<Transaction> findBySourceAccount(BankAccount sourceAccount);

    // Update this method to use 'timestamp' instead of 'transactionDate'
    List<Transaction> findBySourceAccountAndTimestampBetween(
            BankAccount sourceAccount, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
