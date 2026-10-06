package com.banklab.fdservice.batch;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.banklab.fdservice.dto.BatchJobName;
import com.banklab.fdservice.dto.BatchRun;
import com.banklab.fdservice.dto.BatchRunStatus;
import com.banklab.fdservice.dto.EndOfDayResult;
import com.banklab.fdservice.dto.EodStatus;
import com.banklab.fdservice.service.BusinessClockService;
import com.banklab.fdservice.service.FdAccountMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * One end-of-day cycle for the current business date from FD_BUSINESS_CLOCK:
 * ACCRUAL, then MATURITY, then (if neither failed and auto-advance is on) roll the
 * clock forward a day. A FAILED cycle leaves the date where it is, so the next run
 * repeats it — safe, because both jobs are idempotent per account. PARTIAL counts as
 * done: skipped accounts are retried by the next day's run.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EndOfDayService {

    private final BusinessClockService businessClock;
    private final BatchJobService batchJobService;

    /**
     * Serializes whole EOD cycles. BatchJobService only locks one job at a time, so
     * without this a cron EOD and a manual EOD could interleave between ACCRUAL and
     * MATURITY and both advance the clock.
     */
    private final ReentrantLock eodLock = new ReentrantLock();

    @Value("${fd.business-clock.auto-advance-after-eod}")
    private boolean autoAdvance;

    public EndOfDayResult runEndOfDay() {
        if (!eodLock.tryLock()) {
            throw new BatchJobAlreadyRunningException("End of day is already running; retry once it finishes");
        }
        try {
            return runEndOfDayLocked();
        } finally {
            eodLock.unlock();
        }
    }

    private EndOfDayResult runEndOfDayLocked() {
        LocalDate businessDate = businessClock.currentBusinessDate();
        businessClock.markEod(EodStatus.RUNNING);

        BatchRun accrual;
        BatchRun maturity;
        try {
            accrual = batchJobService.run(BatchJobName.ACCRUAL, businessDate);
            maturity = batchJobService.run(BatchJobName.MATURITY, businessDate);
        } catch (RuntimeException e) {
            businessClock.markEod(EodStatus.FAILED);
            throw e;
        }

        boolean failed = accrual.status() == BatchRunStatus.FAILED || maturity.status() == BatchRunStatus.FAILED;
        businessClock.markEod(failed ? EodStatus.FAILED : EodStatus.COMPLETED);
        if (!failed && autoAdvance) {
            businessClock.advance(1);
        }
        log.info("EOD for {} {}; accrual={}, maturity={}", businessDate, failed ? "FAILED" : "completed",
                accrual.status(), maturity.status());
        return new EndOfDayResult(businessDate, accrual, maturity,
                FdAccountMapper.toResponse(businessClock.getClock()));
    }
}
