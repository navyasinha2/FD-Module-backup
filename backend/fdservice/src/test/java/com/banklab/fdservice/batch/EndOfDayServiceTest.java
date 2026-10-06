package com.banklab.fdservice.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.banklab.fdservice.dto.BatchJobName;
import com.banklab.fdservice.dto.BatchRun;
import com.banklab.fdservice.dto.BatchRunStatus;
import com.banklab.fdservice.dto.EndOfDayResult;
import com.banklab.fdservice.dto.EodStatus;
import com.banklab.fdservice.entity.FdBusinessClock;
import com.banklab.fdservice.service.BusinessClockService;

/**
 * Unit tests for the end-of-day cycle: ordering, clock advance, and that two EOD
 * cycles can never overlap.
 */
@ExtendWith(MockitoExtension.class)
class EndOfDayServiceTest {

    private static final LocalDate DAY = LocalDate.parse("2026-02-01");

    @Mock
    private BusinessClockService businessClock;

    @Mock
    private BatchJobService batchJobService;

    @Test
    void secondEndOfDayWhileOneIsRunningIsRejectedAndTheClockAdvancesOnce() throws Exception {
        EndOfDayService service = newService(true);
        when(businessClock.currentBusinessDate()).thenReturn(DAY);
        when(businessClock.getClock()).thenReturn(clockOn(DAY.plusDays(1)));
        CountDownLatch accrualStarted = new CountDownLatch(1);
        CountDownLatch releaseAccrual = new CountDownLatch(1);
        when(batchJobService.run(eq(BatchJobName.ACCRUAL), eq(DAY))).thenAnswer(invocation -> {
            accrualStarted.countDown();
            releaseAccrual.await(5, TimeUnit.SECONDS);
            return run(BatchJobName.ACCRUAL, BatchRunStatus.SUCCESS);
        });
        when(batchJobService.run(eq(BatchJobName.MATURITY), eq(DAY)))
                .thenReturn(run(BatchJobName.MATURITY, BatchRunStatus.SUCCESS));

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<EndOfDayResult> first = pool.submit(service::runEndOfDay);
            assertThat(accrualStarted.await(5, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(service::runEndOfDay).isInstanceOf(BatchJobAlreadyRunningException.class);

            releaseAccrual.countDown();
            assertThat(first.get(5, TimeUnit.SECONDS).businessDate()).isEqualTo(DAY);
        } finally {
            pool.shutdownNow();
        }
        verify(businessClock, times(1)).advance(1);
        verify(businessClock, times(1)).markEod(EodStatus.COMPLETED);
    }

    @Test
    void failedJobMarksEodFailedAndDoesNotAdvanceTheClock() {
        EndOfDayService service = newService(true);
        when(businessClock.currentBusinessDate()).thenReturn(DAY);
        when(businessClock.getClock()).thenReturn(clockOn(DAY));
        when(batchJobService.run(BatchJobName.ACCRUAL, DAY)).thenReturn(run(BatchJobName.ACCRUAL, BatchRunStatus.FAILED));
        when(batchJobService.run(BatchJobName.MATURITY, DAY))
                .thenReturn(run(BatchJobName.MATURITY, BatchRunStatus.SUCCESS));

        EndOfDayResult result = service.runEndOfDay();

        assertThat(result.accrual().status()).isEqualTo(BatchRunStatus.FAILED);
        verify(businessClock).markEod(EodStatus.FAILED);
        verify(businessClock, never()).advance(1);
    }

    private EndOfDayService newService(boolean autoAdvance) {
        EndOfDayService service = new EndOfDayService(businessClock, batchJobService);
        ReflectionTestUtils.setField(service, "autoAdvance", autoAdvance);
        return service;
    }

    private static BatchRun run(BatchJobName job, BatchRunStatus status) {
        return new BatchRun(1L, job, DAY, status, 0, 0, 0, null, null, null);
    }

    private static FdBusinessClock clockOn(LocalDate date) {
        return FdBusinessClock.builder().entityCd("FD").businessDt(date).eodSts("OPEN").build();
    }
}
