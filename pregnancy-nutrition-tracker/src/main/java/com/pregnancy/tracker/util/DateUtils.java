package com.pregnancy.tracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date operations and trimester calculations.
 */
public class DateUtils {

    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Calculate current pregnancy week from start date.
     * @param startDate pregnancy start date (LMP)
     * @return week number (1-40)
     */
    public static int calculateWeek(LocalDate startDate) {
        if (startDate == null) return 0;
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        int weeks = (int) (days / 7) + 1;
        return Math.min(Math.max(weeks, 1), 40);
    }

    /**
     * Calculate trimester from week number.
     * @param week pregnancy week
     * @return trimester (1, 2, or 3)
     */
    public static int calculateTrimester(int week) {
        if (week <= 12) return 1;
        if (week <= 26) return 2;
        return 3;
    }

    /**
     * Calculate trimester from pregnancy start date.
     */
    public static int calculateTrimester(LocalDate startDate) {
        return calculateTrimester(calculateWeek(startDate));
    }

    /**
     * Get estimated due date (40 weeks from LMP).
     * @param startDate pregnancy start date
     * @return estimated due date
     */
    public static LocalDate calculateDueDate(LocalDate startDate) {
        if (startDate == null) return null;
        return startDate.plusWeeks(40);
    }

    /**
     * Get days remaining until due date.
     */
    public static long daysUntilDue(LocalDate startDate) {
        LocalDate dueDate = calculateDueDate(startDate);
        if (dueDate == null) return 0;
        return Math.max(ChronoUnit.DAYS.between(LocalDate.now(), dueDate), 0);
    }

    /**
     * Format a date for display.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DISPLAY_FORMAT);
    }

    /**
     * Parse a date string safely.
     * @param dateStr date string in ISO format (yyyy-MM-dd)
     * @return parsed LocalDate or null
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            System.err.println("[DateUtils] Invalid date: " + dateStr);
            return null;
        }
    }

    /**
     * Get trimester description.
     * @param trimester trimester number
     * @return human-readable trimester description
     */
    public static String getTrimesterDescription(int trimester) {
        return switch (trimester) {
            case 1 -> "1st Trimester (Weeks 1-12)";
            case 2 -> "2nd Trimester (Weeks 13-26)";
            case 3 -> "3rd Trimester (Weeks 27-40)";
            default -> "Unknown";
        };
    }

    /**
     * Get progress percentage through pregnancy.
     * @param week current week
     * @return percentage (0-100)
     */
    public static double getProgressPercentage(int week) {
        return Math.min((week / 40.0) * 100, 100);
    }
}
