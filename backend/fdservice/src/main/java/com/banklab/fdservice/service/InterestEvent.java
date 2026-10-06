package com.banklab.fdservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Interest settled at one compounding or payout boundary, already rounded to the
 * account's currency decimals.
 */
public record InterestEvent(LocalDate boundaryDate, BigDecimal amount, InterestEventType type) {
}
