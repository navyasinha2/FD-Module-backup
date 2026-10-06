package com.banklab.fdservice.dto;

import java.time.LocalDate;

/**
 * Outcome of one end-of-day cycle: the business date processed, both job runs, and
 * the clock afterwards (advanced by a day if EOD succeeded and auto-advance is on).
 */
public record EndOfDayResult(
        LocalDate businessDate,
        BatchRun accrual,
        BatchRun maturity,
        BusinessClockView clock) {
}
