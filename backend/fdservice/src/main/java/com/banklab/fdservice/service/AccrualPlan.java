package com.banklab.fdservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * What an accrual run must write for one account. Pure data — produced by
 * {@link InterestCalculator#planAccrual}, applied by {@link InterestPostingService}.
 *
 * @param noOp                    true when the account is already accrued that far
 *                                (re-run of the same business date)
 * @param events                  capitalizations / payouts due, in date order
 * @param closingPrincipalBalance FDA_PRINCIPAL_BAL after capitalizations
 * @param accruedInterest         new FDA_ACCRUED_INT_AMT (4 decimals)
 * @param accruedThrough          interest is now accrued for every day before this date
 * @param lastSettlementDate      new FDA_LAST_CAPTLZ_DT (unchanged when there are no events)
 */
public record AccrualPlan(
        boolean noOp,
        List<InterestEvent> events,
        BigDecimal closingPrincipalBalance,
        BigDecimal accruedInterest,
        LocalDate accruedThrough,
        LocalDate lastSettlementDate) {

    public AccrualPlan {
        events = List.copyOf(events);
    }

    /**
     * New FDA_LAST_ACCRUAL_DT: the last day whose interest is included (the day before
     * {@link #accruedThrough()}). For the EOD of business date D this is D itself.
     */
    public LocalDate lastAccrualDate() {
        return accruedThrough == null ? null : accruedThrough.minusDays(1);
    }

    static AccrualPlan noOp(AccountInterestState state) {
        return new AccrualPlan(true, List.of(), state.principalBalance(), null, state.accruedThrough(),
                state.lastSettlementDate());
    }
}
