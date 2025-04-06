package com.example.savings.service;

import com.example.savings.model.SavingsGoal;
import com.example.savings.model.SavingsRule;
import com.example.savings.model.Transaction;
import com.example.savings.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("$#,##0.00");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Autowired
    private JavaMailSender emailSender;

    /**
     * Send a notification when a savings rule is triggered
     */
    @Async
    public void sendSavingsRuleTriggeredNotification(User user, SavingsRule rule, Transaction transaction) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                logger.warn("Cannot send notification to user ID {} - no email address", user.getId());
                return;
            }

            SavingsGoal goal = rule.getSavingsGoal();
            double progressPercentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Money saved toward your " + goal.getName() + " goal!");

            StringBuilder body = new StringBuilder();
            body.append("Hello ").append(user.getFirstName()).append(",\n\n");
            body.append("Great news! Your savings rule \"").append(rule.getName()).append("\" was triggered.\n\n");
            body.append("Amount saved: ").append(MONEY_FORMAT.format(transaction.getAmount())).append("\n");
            body.append("Goal: ").append(goal.getName()).append("\n");
            body.append("Current progress: ").append(MONEY_FORMAT.format(goal.getCurrentAmount()))
                .append(" of ").append(MONEY_FORMAT.format(goal.getTargetAmount()))
                .append(" (").append(String.format("%.1f", progressPercentage)).append("%)\n\n");

            if (goal.getTargetDate() != null) {
                body.append("Target date: ").append(goal.getTargetDate().format(DATE_FORMATTER)).append("\n\n");
            }

            body.append("Keep up the great work!\n\n");
            body.append("The Savings Goal Tracker Team");

            message.setText(body.toString());
            emailSender.send(message);

            logger.info("Sent rule triggered notification to user ID {}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to send rule triggered notification to user ID {}: {}",
                    user.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send a notification when a goal is near completion
     */
    @Async
    public void sendGoalNearCompletionNotification(User user, SavingsGoal goal, double progressPercentage) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                logger.warn("Cannot send notification to user ID {} - no email address", user.getId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("You're almost there! Your " + goal.getName() + " goal is nearly complete!");

            StringBuilder body = new StringBuilder();
            body.append("Hello ").append(user.getFirstName()).append(",\n\n");
            body.append("Congratulations! You're almost at your goal!\n\n");
            body.append("Goal: ").append(goal.getName()).append("\n");
            body.append("Current amount: ").append(MONEY_FORMAT.format(goal.getCurrentAmount())).append("\n");
            body.append("Target amount: ").append(MONEY_FORMAT.format(goal.getTargetAmount())).append("\n");
            body.append("Progress: ").append(String.format("%.1f", progressPercentage)).append("%\n\n");

            double remaining = goal.getTargetAmount() - goal.getCurrentAmount();
            body.append("You only need ").append(MONEY_FORMAT.format(remaining)).append(" more to reach your goal!\n\n");

            if (goal.getTargetDate() != null) {
                body.append("Target date: ").append(goal.getTargetDate().format(DATE_FORMATTER)).append("\n\n");
            }

            body.append("Keep up the great work!\n\n");
            body.append("The Savings Goal Tracker Team");

            message.setText(body.toString());
            emailSender.send(message);

            logger.info("Sent goal near completion notification to user ID {}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to send goal near completion notification to user ID {}: {}",
                    user.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send a notification when a goal is completed
     */
    @Async
    public void sendGoalCompletedNotification(User user, SavingsGoal goal) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                logger.warn("Cannot send notification to user ID {} - no email address", user.getId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Congratulations! You've reached your " + goal.getName() + " goal!");

            StringBuilder body = new StringBuilder();
            body.append("Hello ").append(user.getFirstName()).append(",\n\n");
            body.append("CONGRATULATIONS! You've reached your savings goal!\n\n");
            body.append("Goal: ").append(goal.getName()).append("\n");
            body.append("Amount saved: ").append(MONEY_FORMAT.format(goal.getCurrentAmount())).append("\n");

            if (goal.getTargetDate() != null) {
                if (goal.getTargetDate().isAfter(java.time.LocalDate.now())) {
                    body.append("You reached your goal ahead of schedule! Your target date was ")
                        .append(goal.getTargetDate().format(DATE_FORMATTER)).append(".\n\n");
                } else {
                    body.append("You reached your goal on time! Your target date was ")
                        .append(goal.getTargetDate().format(DATE_FORMATTER)).append(".\n\n");
                }
            }

            body.append("This is a huge achievement! What would you like to do next?\n\n");
            body.append("1. Create a new savings goal\n");
            body.append("2. Increase your current goal amount\n");
            body.append("3. Keep your money saved and continue earning interest\n\n");

            body.append("Log in to your account to manage your completed goal.\n\n");
            body.append("Congratulations again on this significant financial milestone!\n\n");
            body.append("The Savings Goal Tracker Team");

            message.setText(body.toString());
            emailSender.send(message);

            logger.info("Sent goal completion notification to user ID {}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to send goal completion notification to user ID {}: {}",
                    user.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send a weekly progress report
     */
    @Async
    public void sendWeeklyProgressReport(User user, SavingsGoal goal, double weeklyAmount, double monthlyAmount) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                logger.warn("Cannot send weekly report to user ID {} - no email address", user.getId());
                return;
            }

            double progressPercentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Your Weekly Savings Progress Report");

            StringBuilder body = new StringBuilder();
            body.append("Hello ").append(user.getFirstName()).append(",\n\n");
            body.append("Here's your weekly savings progress report:\n\n");
            body.append("Goal: ").append(goal.getName()).append("\n");
            body.append("Current progress: ").append(MONEY_FORMAT.format(goal.getCurrentAmount()))
                .append(" of ").append(MONEY_FORMAT.format(goal.getTargetAmount()))
                .append(" (").append(String.format("%.1f", progressPercentage)).append("%)\n\n");

            body.append("Amount saved this week: ").append(MONEY_FORMAT.format(weeklyAmount)).append("\n");
            body.append("Amount saved this month: ").append(MONEY_FORMAT.format(monthlyAmount)).append("\n\n");

            if (goal.getTargetDate() != null) {
                body.append("Target date: ").append(goal.getTargetDate().format(DATE_FORMATTER)).append("\n\n");
            }

            body.append("Keep up the great work!\n\n");
            body.append("The Savings Goal Tracker Team");

            message.setText(body.toString());
            emailSender.send(message);

            logger.info("Sent weekly progress report to user ID {}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to send weekly progress report to user ID {}: {}",
                    user.getId(), e.getMessage(), e);
        }
    }
}
