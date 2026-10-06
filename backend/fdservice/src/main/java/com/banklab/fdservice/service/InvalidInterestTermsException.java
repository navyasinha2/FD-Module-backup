package com.banklab.fdservice.service;

/**
 * The account's snapshotted interest terms can't be used to compute interest — a
 * missing rate, an unknown frequency or day-count convention, or an inconsistent
 * combination (e.g. COMPOUND with no periodic compounding frequency). Maps to 422.
 * In the batch this is the typical per-account skip cause.
 */
public class InvalidInterestTermsException extends RuntimeException {

    public InvalidInterestTermsException(String message) {
        super(message);
    }
}
