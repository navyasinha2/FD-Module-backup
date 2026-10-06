package com.banklab.fdservice.service;

/**
 * Raised when account number generation is requested for a branch code with no
 * FD_ACCOUNT_SEQUENCE row set up.
 */
public class AccountSequenceNotConfiguredException extends RuntimeException {

    public AccountSequenceNotConfiguredException(String branchCd) {
        super("No FD_ACCOUNT_SEQUENCE row configured for branch code '" + branchCd + "'");
    }
}
