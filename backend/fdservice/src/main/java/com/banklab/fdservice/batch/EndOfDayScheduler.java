package com.banklab.fdservice.batch;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.banklab.fdservice.dto.EndOfDayResult;
import com.banklab.fdservice.service.BusinessClockService;

import lombok.extern.slf4j.Slf4j;

/**
 * The nightly end-of-day trigger: 12:00 am every day ({@code fd.batch.eod-cron}) in the
 * bank's time zone ({@code fd.batch.eod-zone}, Asia/Kolkata).
 *
 * At 00:00 on 14 Sep the calendar date is the 14th while the business date is still the
 * 13th, so one EOD cycle runs for 13 Sep — accruing that day's interest — and moves the
 * business date to the 14th. If the service was down for some nights, every missed
 * business date is replayed in order until the business date reaches today. Disable
 * with {@code fd.batch.scheduler.enabled=false}; the manual endpoints keep working.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "fd.batch.scheduler.enabled", havingValue = "true")
public class EndOfDayScheduler {

    /** Safety stop for a clock that is years behind. */
    private static final int MAX_CATCH_UP_CYCLES = 3660;

    private final EndOfDayService endOfDayService;
    private final BusinessClockService businessClock;
    private final ZoneId zone;

    public EndOfDayScheduler(EndOfDayService endOfDayService, BusinessClockService businessClock,
            @Value("${fd.batch.eod-zone}") String zone) {
        this.endOfDayService = endOfDayService;
        this.businessClock = businessClock;
        this.zone = ZoneId.of(zone);
    }

    @Scheduled(cron = "${fd.batch.eod-cron}", zone = "${fd.batch.eod-zone}")
    public void runScheduledEndOfDay() {
        catchUpTo(LocalDate.now(zone));
    }

    /**
     * Runs EOD cycles until the business date reaches {@code today}; returns how many ran.
     * Stops at the first cycle that doesn't move the business date forward (a FAILED job
     * or auto-advance switched off) so tomorrow's trigger retries it. Does nothing when the
     * business date is already on or after today.
     */
    public int catchUpTo(LocalDate today) {
        int cycles = 0;
        try {
            while (cycles < MAX_CATCH_UP_CYCLES && businessClock.currentBusinessDate().isBefore(today)) {
                LocalDate processing = businessClock.currentBusinessDate();
                EndOfDayResult result = endOfDayService.runEndOfDay();
                cycles++;
                if (!result.clock().businessDate().isAfter(processing)) {
                    log.error("EOD for {} did not complete; stopping until the next trigger", processing);
                    break;
                }
            }
            log.info("Scheduled EOD ran {} cycle(s); business date is now {}", cycles,
                    businessClock.currentBusinessDate());
        } catch (RuntimeException e) {
            // Never let an exception kill the scheduler thread; the next trigger retries.
            log.error("Scheduled EOD failed after {} cycle(s)", cycles, e);
        }
        return cycles;
    }
}
