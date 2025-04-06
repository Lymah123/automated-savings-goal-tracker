package com.example.savings.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String firstName;  

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BankAccount> bankAccounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SavingsGoal> savingsGoals;

    public String getFirstName() {
        return name;
    }

    public Optional<User> orElseThrow(Supplier<RuntimeException> exceptionSupplier) {
        return Optional.of(this);
    }
}
