package com.example.savings.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for date operations used throughout the application.
 */
public class DateUtil {

  /**
   * Calculates the number of days between two dates.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return the number of days between the two dates
   */
  public static long daysBetween(LocalDate startDate, LocalDate endDate) {
    return ChronoUnit.DAYS.between(startDate, endDate);
  }

  /**
   * Calculate the number of months between two dates.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return the number of months between the dates
   */
  public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
    return ChronoUnit.MONTHS.between(startDate, endDate);
  }

  /**
   * Estimate a future date based on current progress and target.
   *
   * @param startDate the start date
   * @param currentDate the current date
   * @param currentAmount the current amount saved
   * @param targetAmount the target amount to save
   * @return the estimated completion date
   */
  public static LocalDate estimateCompletionDate(LocalDate startDate, LocalDate currentDate, double currentAmount, double targetAmount) {
    if (currentAmount >= targetAmount) {
      return currentDate;
    }

    if (currentAmount <= 0) {
      return null; // Cannot estimate without progress
    }

    // Calculate daily saving rate
    long daysPassed = daysBetween(startDate, currentDate);
    if (daysPassed <= 0) {
      return null; // Cannot estimate on first date
    }

    double dailyRate = currentAmount / daysPassed;
    double remainingAmount = targetAmount - currentAmount;
    long daysRemaining = (long)Math.ceil(remainingAmount / dailyRate);

    return currentDate.plusDays(daysRemaining);
  }

  /**
   * Get the next occurrence of a specific day of week.
   *
   * @param dayOfWeek the day of week
   * @return the date that falls on the specified day of week
   */
  public static LocalDate getNextDayOfWeek(DayOfWeek dayOfWeek) {
    return LocalDate.now().with(TemporalAdjusters.next(dayOfWeek));
  }

  /**
   * Get the last day of the current month.
   *
   * @return the last day of the current month
   */
  public static LocalDate getLastDayOfMonth() {
    return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
  }

  /**
   * Get the first day of the current month.
   *
   * @return the first day of the current month
   */
  public static LocalDate getFirstDayOfMonth() {
    return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
  }

  /**
   * Get a list of dates for all occurrences of a specific day of week between two dates.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param dayOfWeek the day of week
   * @return list of dates that fall on the specified day of week
   */
  public static List<LocalDate> getDatesOfDayBetween(LocalDate startDate, LocalDate endDate, DayOfWeek dayOfWeek) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate date = startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));

    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusWeeks(1);
    }

    return dates;
  }

  /**
   * Check if the current date is the last day of the month.
   *
   * @return true if today is the last day of the month
   */
  public static boolean isLastDayOfMonth() {
    LocalDate today = LocalDate.now();
    return today.equals(today.with(TemporalAdjusters.lastDayOfMonth()));
  }

  /**
   * Check if the current date is the first day of the month.
   *
   * @return true if today is the first day of teh month
   */
  public static boolean isFirstDayOfMonth() {
    LocalDate today = LocalDate.now();
    return today.getDayOfMonth() == 1;
  }

  /**
   * Convert LocalDateTime to midnight of the same day.
   *
   * @param dateTime the date time to convert
   * @return the same date at midnight (start of day)
   */
  public static LocalDateTime toStartOfDay(LocalDateTime dateTime) {
    return dateTime.toLocalDate().atStartOfDay();
  }

  /**
   * Convert LocalDateTime to the end of the day (23:59:59.999999999).
   *
   * @param dateTime the date time to convert
   * @return the same date at the end of the day
   */
  public static LocalDateTime toEndOfDay(LocalDateTime dateTime) {
    return dateTime.toLocalDate().atTime(23, 59, 59, 999999999);
  }

  /**
   * Get the start and end dates for a specific time period from today.
   *
   * @param period the time period ("week", "month", "year")
   * @return array with start date at index 0 and end date at index 1
   */
  public static LocalDate[] getDateRangeForPeriod(String period) {
    LocalDate today = LocalDate.now();
    LocalDate startDate;
    LocalDate endDate = today;

    switch (period.toLowerCase()) {
      case "week":
        startDate = today.minusWeeks(1);
        break;
      case "month":
        startDate = today.minusMonths(1);
        break;
      case "quarter":
        startDate = today.minusMonths(3);
        break;
      case "year":
        startDate = today.minusYears(1);
        break;
      default:
        startDate = today.minusDays(7);
    }

    return new LocalDate[] { startDate, endDate };
  }
}
