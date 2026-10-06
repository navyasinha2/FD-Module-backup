package com.banklab.fdservice.client;

/**
 * Projection of Group 2's PRODUCTS row needed to book an FD account. Snapshotted
 * onto FD_ACCOUNTS at creation time (ERD principle P1) — never re-fetched afterwards.
 *
 * defaultInterestType (SIMPLE / COMPOUND) is not yet a confirmed field of G2's
 * PRODUCTS contract (ERD §14 open item); it may be null, in which case booking falls
 * back to fd.interest.default-interest-type.
 */
public record ProductDetails(
        String productCode,
        String productName,
        String currency,
        String defaultCompoundingFrequency,
        String defaultPayoutFrequency,
        String defaultMaturityInstruction,
        String defaultInterestType) {
}
