package com.banklab.fdservice.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * FDA_DAY_COUNT_CONV — how an annual rate turns into interest for a run of days.
 *
 * A period [from, to) counts the day money is deposited and not the day it leaves
 * (maturity or withdrawal), which is standard for term deposits. The year fraction is
 * returned as an exact numerator / denominator pair so callers can do a single
 * division at the end and lose no precision.
 */
public enum DayCountConvention {

    /**
     * Actual days ÷ 365 in every year, leap years included (a 29-day leap February
     * earns 29/365). Kept for accounts booked before ACT/ACT became the default.
     */
    ACT_365("ACT/365") {
        @Override
        public long yearFractionNumerator(LocalDate from, LocalDate to) {
            return ChronoUnit.DAYS.between(from, to);
        }

        @Override
        public long yearFractionDenominator() {
            return 365;
        }
    },

    /**
     * Actual days, each divided by the length of the calendar year it falls in —
     * 366 in a leap year, 365 otherwise (ACT/ACT ISDA). A period crossing 31 Dec is
     * split by year: 1 Dec 2027 → 1 Feb 2028 is 31/365 + 31/366. Default for new
     * accounts ({@code fd.interest.day-count-convention}).
     */
    ACT_ACT("ACT/ACT") {
        @Override
        public long yearFractionNumerator(LocalDate from, LocalDate to) {
            long daysInCommonYears = 0;
            long daysInLeapYears = 0;
            LocalDate cursor = from;
            while (cursor.isBefore(to)) {
                LocalDate nextYear = LocalDate.of(cursor.getYear() + 1, 1, 1);
                LocalDate segmentEnd = to.isBefore(nextYear) ? to : nextYear;
                long days = ChronoUnit.DAYS.between(cursor, segmentEnd);
                if (cursor.isLeapYear()) {
                    daysInLeapYears += days;
                } else {
                    daysInCommonYears += days;
                }
                cursor = segmentEnd;
            }
            // d365/365 + d366/366 over the common denominator 365 × 366
            return daysInCommonYears * 366 + daysInLeapYears * 365;
        }

        @Override
        public long yearFractionDenominator() {
            return 365L * 366L;
        }
    };

    private final String code;

    DayCountConvention(String code) {
        this.code = code;
    }

    /** The value stored in FDA_DAY_COUNT_CONV. */
    public String code() {
        return code;
    }

    /** Numerator of the year fraction for [from, to); pair with {@link #yearFractionDenominator()}. */
    public abstract long yearFractionNumerator(LocalDate from, LocalDate to);

    public abstract long yearFractionDenominator();

    public static DayCountConvention fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidInterestTermsException("Day-count convention (FDA_DAY_COUNT_CONV) is not set");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (DayCountConvention convention : values()) {
            if (convention.code.equals(normalized) || convention.name().equals(normalized)) {
                return convention;
            }
        }
        throw new InvalidInterestTermsException("Unsupported day-count convention '" + code + "'");
    }
}
