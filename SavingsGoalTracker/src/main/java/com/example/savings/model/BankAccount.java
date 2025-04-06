package com.example.savings.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity@Table(name = "bank_accounts")
public class BankAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String accountName;

  @Column(nullable = false)
  private String accountNumber;

  @Column(nullable = false)
  private String bankName;

  @Column(nullable = false)
  private String accountType;

  @Column(nullable = false)
  private Double balance;

  @Column(nullable = false)
  private String accessToken;

  @Column
  private String routingNumber;

  public String getRoutingNumber() {
    return routingNumber;
  }
  public void setRoutingNumber(String routingNumber) {
    this.routingNumber = routingNumber;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}

