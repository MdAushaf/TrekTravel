package com.trektravel.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Server-side input validation helpers.
 */
public class Validate {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,8}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

    public static boolean email(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean phone(String phone) {
        if (phone == null) return false;
        String clean = phone.replaceAll("[\\s\\-+]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    /**
     * Password must be at least 8 characters long and contain at least one letter and one number.
     */
    public static boolean password(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean nonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean lengthBetween(String s, int min, int max) {
        if (s == null) return min == 0;
        int len = s.trim().length();
        return len >= min && len <= max;
    }

    public static boolean intBetween(int val, int min, int max) {
        return val >= min && val <= max;
    }

    public static boolean decimalPositive(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Validates that age is between 12 and 70 years old.
     */
    public static boolean ageValid(LocalDate dob) {
        if (dob == null) return false;
        LocalDate today = LocalDate.now();
        if (dob.isAfter(today)) return false;
        int age = Period.between(dob, today).getYears();
        return age >= 12 && age <= 70;
    }

    public static LocalDate date(String str) {
        if (!nonEmpty(str)) return null;
        try {
            return LocalDate.parse(str.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static int intVal(String str, int defaultVal) {
        if (!nonEmpty(str)) return defaultVal;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static BigDecimal decVal(String str, BigDecimal defaultVal) {
        if (!nonEmpty(str)) return defaultVal;
        try {
            return new BigDecimal(str.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
