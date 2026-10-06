package com.banklab.fdservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response of POST /fd-accounts/{accountId}/interest-calculations. The first seven
 * fields are the OpenAPI contract; the rest say how the amount was reached.
 */
public record InterestPreviewResponse(
        BigDecimal principal,
        BigDecimal rate,
        LocalDate periodStart,
        LocalDate periodEnd,
        String dayCountConvention,
        BigDecimal interestAmount,
        String currencyCode,
        InterestType interestType,
        String compoundingFrequency,
        String payoutFrequency,
        long days,
        BigDecimal interestPaidOut,
        BigDecimal maturityValue) {
}
