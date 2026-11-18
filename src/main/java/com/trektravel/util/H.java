package com.trektravel.util;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * HTML Escaping, formatting, CSRF generation, and UI badge helpers for JSP views.
 */
public class H {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("₹#,##0.00");
    private static final DecimalFormat SHORT_MONEY_FORMAT = new DecimalFormat("₹#,##0");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Escapes HTML special characters to prevent Cross-Site Scripting (XSS).
     */
    public static String e(Object val) {
        if (val == null) {
            return "";
        }
        String s = String.valueOf(val);
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;"); break;
                case '<':  sb.append("&lt;"); break;
                case '>':  sb.append("&gt;"); break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default:   sb.append(c); break;
            }
        }
        return sb.toString();
    }

    /**
     * Formats monetary values with Rupee symbol and commas.
     */
    public static String money(Object amount) {
        if (amount == null) return "₹0.00";
        try {
            if (amount instanceof BigDecimal) {
                return MONEY_FORMAT.format(amount);
            } else if (amount instanceof Number) {
                return MONEY_FORMAT.format(((Number) amount).doubleValue());
            } else {
                return MONEY_FORMAT.format(new BigDecimal(amount.toString().trim()));
            }
        } catch (Exception e) {
            return "₹" + e(amount);
        }
    }

    /**
     * Formats rounded monetary values without decimal places.
     */
    public static String moneyShort(Object amount) {
        if (amount == null) return "₹0";
        try {
            if (amount instanceof BigDecimal) {
                return SHORT_MONEY_FORMAT.format(amount);
            } else if (amount instanceof Number) {
                return SHORT_MONEY_FORMAT.format(((Number) amount).doubleValue());
            } else {
                return SHORT_MONEY_FORMAT.format(new BigDecimal(amount.toString().trim()));
            }
        } catch (Exception e) {
            return "₹" + e(amount);
        }
    }

    /**
     * Generates a hidden HTML form input carrying the session's CSRF token.
     */
    public static String csrf(HttpSession session) {
        String token = token(session);
        return "<input type=\"hidden\" name=\"_csrf\" value=\"" + e(token) + "\" />";
    }

    /**
     * Returns the raw CSRF token from the session, or empty string.
     */
    public static String token(HttpSession session) {
        if (session == null) return "";
        Object token = session.getAttribute("_csrf");
        if (token == null) {
            token = session.getAttribute("csrf");
        }
        return (token != null) ? token.toString() : "";
    }

    /**
     * Formats a LocalDate into a friendly display string (e.g. 15 Oct 2026).
     */
    public static String dt(LocalDate date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    /**
     * Generates a color-coded Bootstrap badge for system statuses.
     */
    public static String badge(String status) {
        if (status == null) return "";
        String s = status.toUpperCase().trim();
        String bgClass;
        switch (s) {
            case "APPROVED":
            case "CONFIRMED":
            case "OPEN":
            case "ACTIVE":
            case "EASY":
                bgClass = "bg-success";
                break;
            case "PENDING":
            case "INTERMEDIATE":
            case "MODERATE":
                bgClass = "bg-warning text-dark";
                break;
            case "REJECTED":
            case "CANCELLED":
            case "BLOCKED":
            case "DIFFICULT":
                bgClass = "bg-danger";
                break;
            case "CLOSED":
            case "COMPLETED":
            default:
                bgClass = "bg-secondary";
                break;
        }
        return "<span class=\"badge " + bgClass + "\">" + e(s) + "</span>";
    }

    /**
     * Custom badge for difficulty levels.
     */
    public static String difficultyBadge(String difficulty) {
        if (difficulty == null) return "";
        String d = difficulty.toUpperCase().trim();
        String color;
        String icon;
        switch (d) {
            case "EASY":
                color = "success";
                icon = "🟢";
                break;
            case "MODERATE":
                color = "primary";
                icon = "🟡";
                break;
            case "DIFFICULT":
                color = "danger";
                icon = "🔴";
                break;
            default:
                color = "secondary";
                icon = "⚪";
                break;
        }
        return "<span class=\"badge bg-" + color + "\">" + icon + " " + e(d) + "</span>";
    }
}
