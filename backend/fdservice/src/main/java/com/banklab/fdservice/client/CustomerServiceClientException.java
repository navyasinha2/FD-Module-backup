package com.banklab.fdservice.client;

/**
 * Raised when the Customer/Auth service (Group 1) cannot resolve a customerId,
 * or is unreachable.
 */
public class CustomerServiceClientException extends UpstreamServiceException {

    public CustomerServiceClientException(String message, Reason reason) {
        super(message, reason);
    }

    public CustomerServiceClientException(String message, Reason reason, Throwable cause) {
        super(message, reason, cause);
    }
}
