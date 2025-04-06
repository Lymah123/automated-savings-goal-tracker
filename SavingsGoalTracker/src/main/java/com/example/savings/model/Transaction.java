package com.example.savings.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_goal_id", nullable = false)
    private SavingsGoal savingsGoal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private BankAccount sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_rule_id")
    private SavingsRule savingsRule;

    @Column(nullable = false)
    private String description;

    // Add merchant name field
    @Column
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
