package com.example.savings.service;

import com.example.savings.exception.ResourceNotFoundException;
import com.example.savings.model.BankAccount;
import com.example.savings.model.Transaction;
import com.example.savings.model.User;
import com.example.savings.repository.BankAccountRepository;
import com.example.savings.repository.TransactionRepository;
import com.example.savings.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<BankAccount> findAllAccounts() {
        return bankAccountRepository.findAll();
    }

    // Keep your original method but also add the one expected by other classes
    public Optional<BankAccount> findAccountById(Long id) {
        return bankAccountRepository.findById(id);
    }

    // Add this method to match what's being called
    public Optional<BankAccount> findById(Long id) {
        return bankAccountRepository.findById(id);
    }

    public List<BankAccount> findAccountsByUser(User user) {
        return bankAccountRepository.findByUser(user);
    }

    // Add this method to match what's being called
    public List<BankAccount> getUserAccounts(User user) {
        return bankAccountRepository.findByUser(user);
    }

    public List<BankAccount> findAccountsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return bankAccountRepository.findByUser(user);
    }

    public BankAccount saveBankAccount(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    // Add these methods to match what's being called
    public BankAccount addBankAccount(@Valid BankAccount bankAccount, User user) {
        bankAccount.setUser(user);
        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount linkBankAccount(String accountNumber, String routingNumber, User user) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setRoutingNumber(routingNumber);
        bankAccount.setUser(user);
        // Set other necessary fields
        return bankAccountRepository.save(bankAccount);
    }

    // Keep your original method but also add the one expected by other classes
    public void deleteAccount(Long id) {
        bankAccountRepository.deleteById(id);
    }

    // Add this method to match what's being called
    public void deleteBankAccount(Long id) {
        bankAccountRepository.deleteById(id);
    }

    // Add this method to match what's being called
    public BankAccount refreshAccountBalance(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + id));
        // Implement logic to refresh balance
        // This is just a placeholder - you'll need to implement the actual logic
        return bankAccountRepository.save(account);
    }

    public List<Transaction> getAccountTransactions(BankAccount account, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findBySourceAccountAndTimestampBetween(account, startDate, endDate);
    }

    public List<Transaction> getRecentLargeDeposits(BankAccount account, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = getAccountTransactions(account, startDate, endDate);
        return transactions.stream()
                .filter(t -> t.getAmount() > 0 && t.getAmount() >= 100) // Deposits over $100
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByMerchant(BankAccount account, String merchantName,
                                                      LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = getAccountTransactions(account, startDate, endDate);
        return transactions.stream()
                .filter(t -> merchantName.equalsIgnoreCase(t.getMerchantName()))
                .collect(Collectors.toList());
    }
}
