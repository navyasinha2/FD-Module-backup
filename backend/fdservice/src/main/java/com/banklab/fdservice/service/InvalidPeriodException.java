package com.banklab.fdservice.service;

/**
 * An interest period whose end is before its start. Maps to 400.
 */
public class InvalidPeriodException extends RuntimeException {

    public InvalidPeriodException(String message) {
        super(message);
    }
}
