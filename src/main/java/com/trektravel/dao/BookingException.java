package com.trektravel.dao;

/**
 * Thrown when a trek booking cannot be made (e.g. not enough slots free, batch closed,
 * unapproved trek, or missing trekker profile).
 * The message is safe to show directly to the end user.
 */
public class BookingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
