package com.example.savings.service;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class SavingsSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SavingsSchedulerService.class);

    @Autowired
    private SavingsRuleService savingsRuleService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankApiService bankApiService;

    @Scheduled(cron = "${scheduler.cron.expression}")
    public void processAutomatedSavings() {
        logger.info("Starting automated savings processing");

        // Get all active rules
        List<SavingsRule> activeRules = savingsRuleService.getAllActiveRules();
        logger.info("Found {} active savings rules to process", activeRules.size());

        for (SavingsRule rule : activeRules) {
            try {
                logger.debug("Processing rule: {} (type: {})", rule.getName(), rule.getRuleType());

                // Check if source account has sufficient balance
                BankAccount sourceAccount = rule.getSourceAccount();
                Double currentBalance = bankApiService.getAccountBalance(sourceAccount);

                if (currentBalance <= 0) {
                    logger.warn("Skipping rule {} due to insufficient balance in source account", rule.getId());
                    continue;
                }

                // Process each rule based on its type
                switch (rule.getRuleType()) {
                    case FIXED_AMOUNT:
                        processFixedAmountRule(rule, currentBalance);
                        break;
                    case ROUND_UP:
                        processRoundUpRule(rule);
                        break;
                    case PERCENTAGE_OF_INCOME:
                        processPercentageRule(rule);
                        break;
                    case SPENDING_CATEGORY:
                        processSpendingCategoryRule(rule);
                        break;
                    default:
                        logger.warn("Unknown rule type for rule {}: {}", rule.getId(), rule.getRuleType());
                }
            } catch (Exception e) {
                logger.error("Error processing rule " + rule.getId(), e);
            }
        }

        logger.info("Completed automated savings processing");
    }

    private void processFixedAmountRule(SavingsRule rule, Double currentBalance) {
        Double amount = rule.getAmount();

        // Ensure we don't overdraw the account
        if (amount > currentBalance) {
            logger.warn("Reducing transfer amount for rule {} due to insufficient balance", rule.getId());
            amount = currentBalance * 0.9; // Transfer 90% of available balance as a safety measure

            // If the adjusted amount is too small, skip the transfer
            if (amount < 1.0) {
                logger.warn("Skipping transfer for rule {} as adjusted amount is too small", rule.getId());
                return;
            }
        }

        logger.info("Processing fixed amount rule: transferring ${} for rule {}", amount, rule.getId());

        transactionService.createTransaction(
            rule.getSavingsGoal(),
            rule.getSourceAccount(),
            rule,
            amount,
            "Automated savings: " + rule.getName()
        );
    }

    private void processRoundUpRule(SavingsRule rule) {
        // Get transactions from the last day for the source account
        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        // Fetch recent transactions from bank API
        List<Map<String, Object>> recentTransactions =
            bankApiService.getTransactionsSince(rule.getSourceAccount(), yesterday);

        double roundUpTotal = 0.0;

        for (Map<String, Object> transaction : recentTransactions) {
            // Only process debit transactions (money going out)
            if ("debit".equals(transaction.get("type"))) {
                Double amount = (Double) transaction.get("amount");

                // Calculate round-up amount (to next dollar)
                double cents = amount % 1;
                if (cents > 0) {
                    double roundUpAmount = 1 - cents;
                    roundUpTotal += roundUpAmount;
                }
            }
        }

        // Only create a transaction if we have round-ups to process
        if (roundUpTotal > 0) {
            logger.info("Processing round-up rule: transferring ${} for rule {}",
                      roundUpTotal, rule.getId());

            transactionService.createTransaction(
                rule.getSavingsGoal(),
                rule.getSourceAccount(),
                rule,
                roundUpTotal,
                "Round-up savings: " + rule.getName()
            );
        } else {
            logger.debug("No round-up amount to transfer for rule {}", rule.getId());
        }
    }

    private void processPercentageRule(SavingsRule rule) {
        // Get transactions from the last month for income detection
        LocalDateTime lastMonth = LocalDateTime.now().minus(30, ChronoUnit.DAYS);

        // Fetch recent transactions from bank API
        List<Map<String, Object>> recentTransactions =
            bankApiService.getTransactionsSince(rule.getSourceAccount(), lastMonth);

        double totalIncome = 0.0;

        // Identify income deposits (credits to the account)
        for (Map<String, Object> transaction : recentTransactions) {
            if ("credit".equals(transaction.get("type"))) {
                // Check if this is likely an income deposit
                String description = (String) transaction.get("description");
                Double amount = (Double) transaction.get("amount");

                // Look for common income-related keywords
                if (description.toLowerCase().contains("salary") ||
                    description.toLowerCase().contains("payroll") ||
                    description.toLowerCase().contains("direct deposit")) {

                    totalIncome += amount;
                }
            }
        }

        if (totalIncome > 0) {
            // Parse the percentage from rule condition
            double percentage = Double.parseDouble(rule.getRuleCondition());
            double savingsAmount = totalIncome * (percentage / 100);

            // Ensure minimum transfer amount
            if (savingsAmount >= 1.0) {
                logger.info("Processing income percentage rule: transferring ${} ({}% of ${}) for rule {}",
                          savingsAmount, percentage, totalIncome, rule.getId());

                transactionService.createTransaction(
                    rule.getSavingsGoal(),
                    rule.getSourceAccount(),
                    rule,
                    savingsAmount,
                    "Income percentage savings: " + rule.getName()
                );
            } else {
                logger.debug("Skipping income percentage transfer - amount too small: ${}", savingsAmount);
            }
        } else {
            logger.debug("No income detected for percentage rule {}", rule.getId());
        }
    }

    private void processSpendingCategoryRule(SavingsRule rule) {
        // Get transactions from the last week
        LocalDateTime lastWeek = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        // Fetch recent transactions from bank API
        List<Map<String, Object>> recentTransactions =
            bankApiService.getTransactionsSince(rule.getSourceAccount(), lastWeek);

        String targetCategory = rule.getRuleCondition().toLowerCase(); // e.g., "coffee", "dining", "shopping"
        double categorySpending = 0.0;

        // Calculate total spending in the target category
        for (Map<String, Object> transaction : recentTransactions) {
            if ("debit".equals(transaction.get("type"))) {
                String category = (String) transaction.get("category");
                String merchant = (String) transaction.get("merchant");
                Double amount = (Double) transaction.get("amount");

                // Check if transaction matches the target category
                if ((category != null && category.toLowerCase().contains(targetCategory)) ||
                    (merchant != null && merchant.toLowerCase().contains(targetCategory))) {
                    categorySpending += amount;
                }
            }
        }

        if (categorySpending > 0) {
            // Calculate savings amount based on rule amount (percentage of category spending)
            double percentage = rule.getAmount();
            double savingsAmount = categorySpending * (percentage / 100);

            // Ensure minimum transfer amount
            if (savingsAmount >= 1.0) {
                logger.info("Processing category spending rule: transferring ${} ({}% of ${} spent on {}) for rule {}",
                          savingsAmount, percentage, categorySpending, targetCategory, rule.getId());

                transactionService.createTransaction(
                    rule.getSavingsGoal(),
                    rule.getSourceAccount(),
                    rule,
                    savingsAmount,
                    "Category spending savings (" + targetCategory + "): " + rule.getName()
                );
            } else {
                logger.debug("Skipping category spending transfer - amount too small: ${}", savingsAmount);
            }
        } else {
            logger.debug("No spending detected in category '{}' for rule {}", targetCategory, rule.getId());
        }
    }
}
