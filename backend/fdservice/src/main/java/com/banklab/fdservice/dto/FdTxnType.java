package com.banklab.fdservice.dto;

/**
 * FDT_TXN_TYP values (ERD §5).
 */
public enum FdTxnType {
    DEPOSIT,
    INTEREST,
    WITHDRAWAL,
    PENALTY,
    TDS,
    MATURITY_PAYOUT,
    RENEWAL_TRANSFER
}
