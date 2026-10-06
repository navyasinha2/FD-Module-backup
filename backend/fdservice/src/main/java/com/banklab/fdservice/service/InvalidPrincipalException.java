package com.banklab.fdservice.service;

/**
 * A principal amount the account can't hold exactly — more decimal places than the
 * currency (or the DECIMAL(18,2) column) allows. Maps to 400.
 */
public class InvalidPrincipalException extends RuntimeException {

    public InvalidPrincipalException(String message) {
        super(message);
    }
}
