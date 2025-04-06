package com.example.savings.repository;

import com.example.savings.model.BankAccount;
import com.example.savings.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUser(User user);
}
