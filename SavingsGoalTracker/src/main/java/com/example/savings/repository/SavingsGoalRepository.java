package com.example.savings.repository;

import com.example.savings.model.SavingsGoal;
import com.example.savings.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(User user);
}
