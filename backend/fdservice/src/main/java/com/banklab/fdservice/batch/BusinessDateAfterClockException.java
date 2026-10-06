package com.banklab.fdservice.batch;

import java.time.LocalDate;

/**
 * A batch run was requested for a business date the business clock hasn't reached.
 * Running ahead would stamp FDA_LAST_ACCRUAL_DT in the future and let later
 * operations on the real business date credit interest that isn't earned yet.
 * Maps to 409.
 */
public class BusinessDateAfterClockException extends RuntimeException {

    public BusinessDateAfterClockException(LocalDate requested, LocalDate clock) {
        super("Business date " + requested + " is after the business clock (" + clock
                + "); advance the clock first");
    }
}
