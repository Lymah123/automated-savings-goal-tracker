package com.example.savings.service;

import com.example.savings.model.BankAccount;
import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.Transaction;
import com.example.savings.repository.SavingsRuleRepository;
import com.example.savings.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavingsAutomationService {
    private static final Logger logger = LoggerFactory.getLogger(SavingsAutomationService.class);

    @Autowired
    private SavingsRuleRepository savingsRuleRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Process daily savings rules - runs at 1:00 AM every day
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processDailyRules() {
        logger.info("Processing daily savings rules");
        List<SavingsRule> dailyRules = savingsRuleRepository.findByRuleConditionAndIsActive("DAILY", true);
        processRules(dailyRules, "daily");
    }

    /**
     * Process weekly savings rules - runs at 2:00 AM every Monday
     */
    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    public void processWeeklyRules() {
        logger.info("Processing weekly savings rules");
        List<SavingsRule> weeklyRules = savingsRuleRepository.findByRuleConditionAndIsActive("WEEKLY", true);
        processRules(weeklyRules, "weekly");
    }

    /**
     * Process monthly savings rules - runs at 3:00 AM on the 1st day of each month
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional
    public void processMonthlyRules() {
        logger.info("Processing monthly savings rules");
        List<SavingsRule> monthlyRules = savingsRuleRepository.findByRuleConditionAndIsActive("MONTHLY", true);
        processRules(monthlyRules, "monthly");
    }

    /**
     * Process payday savings rules - runs daily at 4:00 AM to check for payday deposits
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void processPaydayRules() {
        logger.info("Checking for payday deposits");

        // Get all active payday rules
        List<SavingsRule> paydayRules = savingsRuleRepository.findByRuleTypeAndIsActive(SavingsRule.RuleType.INCOME_PERCENTAGE, true);

        for (SavingsRule rule : paydayRules) {
            try {
                // Check for recent large deposits in the source account
                BankAccount sourceAccount = rule.getSourceAccount();
                List<Transaction> recentDeposits = bankAccountService.getRecentLargeDeposits(
                    sourceAccount,
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now()
                );

                if (!recentDeposits.isEmpty()) {
                    // Process each deposit with the income percentage rule
                    for (Transaction deposit : recentDeposits) {
                        double savingsAmount = deposit.getAmount() * (rule.getAmount() / 100.0);

                        // Create the savings transaction
                        Transaction savingsTransaction = transactionService.createTransaction(
                            rule.getSavingsGoal(),
                            sourceAccount,
                            rule,
                            savingsAmount,
                            "Automatic " + rule.getAmount() + "% from deposit of $" + deposit.getAmount()
                        );

                        // Notify the user
                        notificationService.sendSavingsRuleTriggeredNotification(
                            rule.getSavingsGoal().getUser(),
                            rule,
                            savingsTransaction
                        );
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing payday rule ID {}: {}", rule.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Process round-up savings rules - runs at 5:00 AM daily
     */
    @Scheduled(cron = "0 0 5 * * ?")
    @Transactional
    public void processRoundUpRules() {
        logger.info("Processing round-up rules");

        // Get all active round-up rules
        List<SavingsRule> roundUpRules = savingsRuleRepository.findByRuleTypeAndIsActive(SavingsRule.RuleType.ROUND_UP, true);

        for (SavingsRule rule : roundUpRules) {
            try {
                // Get yesterday's transactions from the source account
                BankAccount sourceAccount = rule.getSourceAccount();
                LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
                LocalDateTime endOfYesterday = LocalDateTime.now().minusDays(1).toLocalDate().atTime(23, 59, 59);

                List<Transaction> yesterdayTransactions = bankAccountService.getAccountTransactions(
                    sourceAccount,
                    startOfYesterday,
                    endOfYesterday
                );

                // Calculate total round-up amount
                double totalRoundUp = 0.0;
                for (Transaction transaction : yesterdayTransactions) {
                    // Only consider purchase transactions (negative amounts)
                    if (transaction.getAmount() < 0) {
                        double transactionAmount = Math.abs(transaction.getAmount());
                        double roundUpAmount = Math.ceil(transactionAmount) - transactionAmount;
                        totalRoundUp += roundUpAmount;
                    }
                }

                // If we have round-ups to process
                if (totalRoundUp > 0) {
                    // Create the savings transaction
                    Transaction savingsTransaction = transactionService.createTransaction(
                        rule.getSavingsGoal(),
                        sourceAccount,
                        rule,
                        totalRoundUp,
                        "Round-up savings from " + yesterdayTransactions.size() + " transactions"
                    );

                    // Notify the user
                    notificationService.sendSavingsRuleTriggeredNotification(
                        rule.getSavingsGoal().getUser(),
                        rule,
                        savingsTransaction
                    );
                }
            } catch (Exception e) {
                logger.error("Error processing round-up rule ID {}: {}", rule.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Process custom trigger rules - runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void processCustomTriggerRules() {
        logger.info("Processing custom trigger rules");

        // Get all active custom trigger rules
        List<SavingsRule> customRules = savingsRuleRepository.findByRuleTypeAndIsActive(SavingsRule.RuleType.CUSTOM_TRIGGER, true);

        for (SavingsRule rule : customRules) {
            try {
                // Parse the rule condition - this would be more complex in a real app
                // For this example, we'll use a simple format like "MERCHANT:Starbucks"
                String condition = rule.getRuleCondition();
                if (condition != null && condition.startsWith("MERCHANT:")) {
                    String merchant = condition.substring("MERCHANT:".length());

                    // Check for transactions with this merchant in the last hour
                    BankAccount sourceAccount = rule.getSourceAccount();
                    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

                    List<Transaction> recentTransactions = bankAccountService.getTransactionsByMerchant(
                        sourceAccount,
                        merchant,
                        oneHourAgo,
                        LocalDateTime.now()
                    );

                    // If we found matching transactions, trigger the rule
                    if (!recentTransactions.isEmpty()) {
                        // Create the savings transaction
                        Transaction savingsTransaction = transactionService.createTransaction(
                            rule.getSavingsGoal(),
                            sourceAccount,
                            rule,
                            rule.getAmount(),
                            "Automatic savings triggered by purchase at " + merchant
                        );

                        // Notify the user
                        notificationService.sendSavingsRuleTriggeredNotification(
                            rule.getSavingsGoal().getUser(),
                            rule,
                            savingsTransaction
                        );
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing custom trigger rule ID {}: {}", rule.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Execute all automated savings rules - can be called manually or scduled
     */
    @Transactional
    public void executeAutomatedSavings() {
      logger.info("Executing all automated savings rules");

      // Process each type of rule
      processDailyRules();
      processWeeklyRules();
      processMonthlyRules();
      processPaydayRules();
      processRoundUpRules();
      processCustomTriggerRules();
      logger.info("All automated savings rules executed");
    }

    /**
     * Helper method to process a list of rules
     */
    private void processRules(List<SavingsRule> rules, String frequency) {
        for (SavingsRule rule : rules) {
            try {
                if (rule.getRuleType() == SavingsRule.RuleType.FIXED_AMOUNT) {
                    SavingsGoal goal = rule.getSavingsGoal();
                    BankAccount sourceAccount = rule.getSourceAccount();

                    // Create the transaction
                    Transaction transaction = transactionService.createTransaction(
                        goal,
                        sourceAccount,
                        rule,
                        rule.getAmount(),
                        "Automatic " + frequency + " savings of $" + rule.getAmount()
                    );

                    // Notify the user
                    notificationService.sendSavingsRuleTriggeredNotification(
                        goal.getUser(),
                        rule,
                        transaction
                    );

                    logger.info("Processed {} rule ID {}: ${} saved to goal '{}'",
                        frequency, rule.getId(), rule.getAmount(), goal.getName());
                }
            } catch (Exception e) {
                logger.error("Error processing {} rule ID {}: {}", frequency, rule.getId(), e.getMessage(), e);
            }
        }
    }

    /**
 * Check for goals that are close to completion - runs daily at 6:00 AM
 */
@Scheduled(cron = "0 0 6 * * ?")
public void checkGoalProgress() {
    logger.info("Checking goal progress");

    // Get rules for goals that are near completion
    List<SavingsRule> rulesWithNearCompleteGoals = savingsRuleRepository.findActiveGoalsNearCompletion(0.90); // 90% or more complete

    // Extract the unique goals from these rules
    List<SavingsGoal> activeGoals = rulesWithNearCompleteGoals.stream()
        .map(SavingsRule::getSavingsGoal)
        .distinct()
        .collect(java.util.stream.Collectors.toList());

    for (SavingsGoal goal : activeGoals) {
        try {
            double progressPercentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;
            if (progressPercentage >= 95) {
                notificationService.sendGoalNearCompletionNotification(
                    goal.getUser(),
                    goal,
                    progressPercentage
                );
                logger.info("Goal '{}' is {}% complete", goal.getName(), String.format("%.1f", progressPercentage));
            }
        } catch (Exception e) {
            logger.error("Error checking progress for goal ID {}: {}", goal.getId(), e.getMessage(), e);
        }
    }
}

}
