package com.banklab.fdservice.client;

/**
 * Projection of Group 1's customer record needed to book an FD account.
 * {@code fullName} is snapshotted onto FD_ACCOUNTS.FDA_CUST_NAME_SNAP at
 * creation time (ERD principle P1/P6) — never re-fetched afterwards.
 */
public record CustomerDto(
        String custId,
        String fullName,
        String status) {
}
