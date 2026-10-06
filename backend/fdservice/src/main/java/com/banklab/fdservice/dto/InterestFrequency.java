package com.banklab.fdservice.dto;

import java.util.Locale;

import com.banklab.fdservice.service.InvalidInterestTermsException;

/**
 * Values of FDA_COMPOUND_FREQ / FDA_PAYOUT_FREQ. Kept as one enum because both
 * columns hold the same vocabulary — but the two columns are never interchangeable
 * (ERD §4: conflating them is "the classic FD bug").
 */
public enum InterestFrequency {
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    ANNUALLY(12),
    /** Nothing happens until maturity. Only meaningful as a payout frequency. */
    ON_MATURITY(0);

    private final int months;

    InterestFrequency(int months) {
        this.months = months;
    }

    public int months() {
        return months;
    }

    public boolean isPeriodic() {
        return months > 0;
    }

    /**
     * Lenient parse of the stored string: blank means "not set" (null), and the common
     * spellings YEARLY / MATURITY are accepted as aliases.
     */
    public static InterestFrequency parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "YEARLY" -> ANNUALLY;
            case "MATURITY" -> ON_MATURITY;
            default -> {
                try {
                    yield InterestFrequency.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    throw new InvalidInterestTermsException("Unsupported interest frequency '" + value + "'");
                }
            }
        };
    }
}
