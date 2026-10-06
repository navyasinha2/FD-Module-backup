package com.banklab.fdservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import com.banklab.fdservice.dto.InterestFrequency;
import com.banklab.fdservice.dto.InterestType;
import com.banklab.fdservice.entity.FdAccount;

/**
 * The snapshotted terms interest is computed from (ERD P1: always the account's own
 * copy, never Group 2's live rate matrix).
 *
 * @param annualRatePct       FDA_INT_RT as a percentage, e.g. 6.5000
 * @param interestType        FDA_INT_TYP
 * @param compoundingFrequency FDA_COMPOUND_FREQ — drives capitalization, COMPOUND only
 * @param payoutFrequency     FDA_PAYOUT_FREQ — drives payouts, SIMPLE only; null or
 *                            ON_MATURITY means interest is held until maturity
 * @param dayCountConvention  FDA_DAY_COUNT_CONV
 * @param currencyDecimals    FDA_CCY_DECIMALS — rounding precision of posted amounts
 */
public record InterestTerms(
        BigDecimal annualRatePct,
        InterestType interestType,
        InterestFrequency compoundingFrequency,
        InterestFrequency payoutFrequency,
        DayCountConvention dayCountConvention,
        int currencyDecimals) {

    public InterestTerms {
        if (annualRatePct == null) {
            throw new InvalidInterestTermsException("Contracted interest rate (FDA_INT_RT) is not set");
        }
        if (annualRatePct.signum() < 0) {
            throw new InvalidInterestTermsException("Interest rate cannot be negative: " + annualRatePct);
        }
        if (interestType == null) {
            throw new InvalidInterestTermsException("Interest type (FDA_INT_TYP) is not set");
        }
        if (dayCountConvention == null) {
            throw new InvalidInterestTermsException("Day-count convention (FDA_DAY_COUNT_CONV) is not set");
        }
        if (interestType == InterestType.COMPOUND
                && (compoundingFrequency == null || !compoundingFrequency.isPeriodic())) {
            throw new InvalidInterestTermsException(
                    "COMPOUND interest needs a periodic compounding frequency, got " + compoundingFrequency);
        }
        if (currencyDecimals < 0 || currencyDecimals > 4) {
            throw new InvalidInterestTermsException("Unsupported currency decimals: " + currencyDecimals);
        }
    }

    /**
     * Terms from an account row. Accounts booked before FDA_INT_TYP was wired through
     * account creation hold NULL there and fall back to {@code defaultInterestType}.
     */
    public static InterestTerms fromAccount(FdAccount account, InterestType defaultInterestType) {
        InterestType type = parseInterestType(account.getIntTyp(), defaultInterestType);
        int decimals = account.getCcyDecimals() != null
                ? account.getCcyDecimals()
                : CurrencyDecimals.forCurrency(account.getCcyCd());
        return new InterestTerms(
                account.getIntRt(),
                type,
                InterestFrequency.parse(account.getCompoundFreq()),
                InterestFrequency.parse(account.getPayoutFreq()),
                DayCountConvention.fromCode(account.getDayCountConv()),
                decimals);
    }

    public static InterestType parseInterestType(String value, InterestType fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return InterestType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidInterestTermsException("Unsupported interest type '" + value + "'");
        }
    }

    /**
     * Capitalization vs. payout: COMPOUND capitalizes at compounding boundaries, SIMPLE
     * pays out at payout boundaries, SIMPLE held to maturity has no boundaries at all.
     */
    public InterestEventType settlementEventType() {
        if (interestType == InterestType.COMPOUND) {
            return InterestEventType.CAPITALIZATION;
        }
        return payoutFrequency != null && payoutFrequency.isPeriodic() ? InterestEventType.PAYOUT : null;
    }

    public InterestFrequency settlementFrequency() {
        return switch (interestType) {
            case COMPOUND -> compoundingFrequency;
            case SIMPLE -> payoutFrequency;
        };
    }

    public BigDecimal roundToCurrency(BigDecimal amount) {
        return amount.setScale(currencyDecimals, RoundingMode.HALF_UP);
    }
}
