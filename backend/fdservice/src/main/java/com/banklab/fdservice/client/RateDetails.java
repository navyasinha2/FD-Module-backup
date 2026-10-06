package com.banklab.fdservice.client;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection of Group 2's PRODUCT_RATE_MATRIX row identified by rateId. The rate is
 * snapshotted onto FD_ACCOUNTS.FDA_INT_RT at booking; rateId itself is kept for
 * traceability back to the matrix row that produced it.
 */
public record RateDetails(
        String rateId,
        String productCode,
        Integer minTenureMonths,
        BigDecimal effectiveRate,
        LocalDate effectiveDate,
        LocalDate expiryDate) {
}
