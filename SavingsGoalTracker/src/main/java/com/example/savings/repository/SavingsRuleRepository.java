package com.example.savings.repository;

import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRuleRepository extends JpaRepository<SavingsRule, Long> {
    List<SavingsRule> findBySavingsGoal(SavingsGoal savingsGoal);

    List<SavingsRule> findByRuleConditionAndIsActive(String ruleCondition, boolean isActive);

    List<SavingsRule> findByRuleTypeAndIsActive(SavingsRule.RuleType ruleType, boolean isActive);

    List<SavingsRule> findByIsActiveTrue();

    @Query("SELECT sr FROM SavingsRule sr JOIN sr.savingsGoal sg WHERE sg.currentAmount/sg.targetAmount >= :threshold AND sr.isActive = true")
    List<SavingsRule> findActiveGoalsNearCompletion(@Param("threshold") double threshold);
}
