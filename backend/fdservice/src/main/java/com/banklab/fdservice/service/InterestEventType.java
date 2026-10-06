package com.banklab.fdservice.service;

/**
 * What happens to interest at a settlement boundary.
 */
public enum InterestEventType {
    /** Added to FDA_PRINCIPAL_BAL — no money leaves the deposit. */
    CAPITALIZATION,
    /** Paid to the customer — FDA_PRINCIPAL_BAL is unchanged. */
    PAYOUT
}
