package com.banklab.fdservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.banklab.fdservice.entity.FdAccount;

/**
 * The mutable interest-related state of one account that accrual planning reads.
 *
 * @param principalBalance   FDA_PRINCIPAL_BAL — the base interest accrues on
 * @param valueDate          FDA_VALUE_DT — interest starts here; boundaries are anchored on it
 * @param maturityDate       FDA_MAT_DT — accrual never runs past it
 * @param lastAccrualDate    FDA_LAST_ACCRUAL_DT — the last day whose interest has been
 *                           accrued (the business date of the last EOD that processed
 *                           the account); the idempotency guard for the accrual job
 * @param lastSettlementDate FDA_LAST_CAPTLZ_DT — last boundary at which interest was
 *                           capitalized or paid out; the current period starts here
 */
public record AccountInterestState(
        BigDecimal principalBalance,
        LocalDate valueDate,
        LocalDate maturityDate,
        LocalDate lastAccrualDate,
        LocalDate lastSettlementDate) {

    public AccountInterestState {
        if (principalBalance == null) {
            throw new InvalidInterestTermsException("Principal balance is not set");
        }
        if (valueDate == null || maturityDate == null) {
            throw new InvalidInterestTermsException("Value date and maturity date must both be set");
        }
    }

    /** Exclusive end of what is already accrued (the day after lastAccrualDate, capped at maturity). */
    public LocalDate accruedThrough() {
        if (lastAccrualDate == null) {
            return null;
        }
        LocalDate dayAfter = lastAccrualDate.plusDays(1);
        return dayAfter.isAfter(maturityDate) ? maturityDate : dayAfter;
    }

    public static AccountInterestState from(FdAccount account) {
        return new AccountInterestState(
                account.getPrincipalBal() != null ? account.getPrincipalBal() : account.getPrincipalAmt(),
                account.getValueDt() != null ? account.getValueDt() : account.getOpenDt(),
                account.getMatDt(),
                account.getLastAccrualDt(),
                account.getLastCaptlzDt());
    }
}
