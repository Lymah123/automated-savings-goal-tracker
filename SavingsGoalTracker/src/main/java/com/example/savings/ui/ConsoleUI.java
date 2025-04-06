package com.example.savings.ui;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.User;
import com.example.savings.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.Optional;

@Component
@Profile("console")
public class ConsoleUI implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private SavingsRuleService savingsRuleService;

    @Autowired
    private SavingsAutomationService automationService;

    private Scanner scanner = new Scanner(System.in);
    private User currentUser = null;

    @Override
    public void run(String... args) {
        System.out.println("Welcome to Automated Savings Goal Tracker");

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }

        scanner.close();
        System.out.println("Application terminated.");
    }

    private void showLoginMenu() {
        System.out.println("\n===== Login/Register =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 0:
                System.exit(0);
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n===== Main Menu =====");
        System.out.println("Welcome, " + currentUser.getName() + "!");
        System.out.println("1. Manage Bank Accounts");
        System.out.println("2. Manage Savings Goals");
        System.out.println("3. Manage Savings Rules");
        System.out.println("4. Run Automated Savings");
        System.out.println("5. Logout");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                manageBankAccounts();
                break;
            case 2:
                manageSavingsGoals();
                break;
            case 3:
                manageSavingsRules();
                break;
            case 4:
                runAutomatedSavings();
                break;
            case 5:
                logout();
                break;
            case 0:
                System.exit(0);
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            // Proper authentication with password hashing
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (passwordEncoder.matches(password, user.getPassword())) {
                  currentUser = user;
                  System.out.println("Login successful!");
            } else {
                System.out.println("Invalid password.");
            }
        } else {
            System.out.println("User not found.");
        }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    private void register() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        try {
            User user = new User();
            user.setName(name);
            user.setUsername(username);
            // Encode the password before saving
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            userService.saveUser(user);
            System.out.println("Registration successful! Please login.");
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
        }
    }

    private void logout() {
        currentUser = null;
        System.out.println("Logged out successfully.");
    }

    private void manageBankAccounts() {
        System.out.println("\n===== Bank Account Management =====");
        System.out.println("1. List accounts");
        System.out.println("2. Add account");
        System.out.println("3. Update account balance");
        System.out.println("0. Back to main menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                listBankAccounts();
                break;
            case 2:
                addBankAccount();
                break;
            case 3:
                updateAccountBalance();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void listBankAccounts() {
        List<BankAccount> accounts = bankAccountService.findAccountsByUser(currentUser);
        if (accounts.isEmpty()) {
            System.out.println("No bank accounts found.");
        } else {
            System.out.println("\nYour Bank Accounts:");
            for (BankAccount account : accounts) {
                System.out.println("ID: " + account.getId() +
                                  ", Name: " + account.getAccountName() +
                                  ", Balance: $" + account.getBalance());
            }
        }
    }

    private void addBankAccount() {
        System.out.print("Enter account name: ");
        String name = scanner.nextLine();
        System.out.print("Enter initial balance: $");
        double balance = getDoubleInput();

        try {
            BankAccount account = new BankAccount();
            account.setAccountName(name);
            account.setBalance(balance);
            account.setUser(currentUser);

            bankAccountService.saveBankAccount(account);
            System.out.println("Bank account added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding bank account: " + e.getMessage());
        }
    }

    private void updateAccountBalance() {
        listBankAccounts();
        System.out.print("Enter account ID to update: ");
        Long accountId = getLongInput();
        System.out.print("Enter new balance: $");
        double balance = getDoubleInput();

        try {
            Optional<BankAccount> accountOpt = bankAccountService.findAccountById(accountId);
            if (accountOpt.isPresent() && accountOpt.get().getUser().getId().equals(currentUser.getId())) {
                BankAccount account = accountOpt.get();
                bankAccountService.saveBankAccount(account);
                System.out.println("Balance updated successfully!");
            } else {
                System.out.println("Account not found or not accessible.");
            }
        } catch (Exception e) {
            System.out.println("Error updating account: " + e.getMessage());
        }
    }

    private void manageSavingsGoals() {
        // Implementation for savings goals management
        System.out.println("\nSavings goals management - To be implemented");
    }

    private void manageSavingsRules() {
        // Implementation for savings rules management
        System.out.println("\nSavings rules management - To be implemented");
    }

    private void runAutomatedSavings() {
        System.out.println("\nRunning automated savings process...");
        try {
            automationService.executeAutomatedSavings();
            System.out.println("Automated savings completed successfully!");
        } catch (Exception e) {
            System.out.println("Error during automated savings: " + e.getMessage());
        }
    }

    // Utility methods for input handling
    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long getLongInput() {
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
