package com.example.savings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SavingsApplication {
  public static void main(String[] args) {
    SpringApplication.run(SavingsApplication.class, args);
  }
}
