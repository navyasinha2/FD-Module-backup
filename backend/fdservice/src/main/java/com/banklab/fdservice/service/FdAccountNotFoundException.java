package com.banklab.fdservice.service;

import java.util.UUID;

/**
 * No FD_ACCOUNTS row for the requested FDA_ID. Maps to 404.
 */
public class FdAccountNotFoundException extends RuntimeException {

    public FdAccountNotFoundException(UUID fdaId) {
        super("FD account not found for accountId=" + fdaId);
    }
}
