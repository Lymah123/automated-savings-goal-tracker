package com.example.savings.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "savings_rules")
public class SavingsRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Column(nullable = false)
    private String ruleCondition;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_goal_id", nullable = false)
    private SavingsGoal savingsGoal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private BankAccount sourceAccount;

    public enum RuleType {
        ROUND_UP,
        PERCENTAGE_OF_INCOME,
        FIXED_AMOUNT,
        SPENDING_CATEGORY,
        INCOME_PERCENTAGE,
        CUSTOM_TRIGGER
    }
}
