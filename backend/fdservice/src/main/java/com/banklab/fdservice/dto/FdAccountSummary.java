package com.banklab.fdservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Matches openapi schema FdAccountSummary (GET /fd-accounts search results).
 */
public record FdAccountSummary(
        String fdaId,
        String acctNum,
        String status,
        BigDecimal principalBal,
        LocalDate matDt,
        String productCode,
        String custNameSnap) {
}
