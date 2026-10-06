package com.banklab.fdservice.batch;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.ToLongFunction;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.banklab.fdservice.dto.BatchJobName;
import com.banklab.fdservice.dto.BatchRun;
import com.banklab.fdservice.dto.BatchRunStatus;
import com.banklab.fdservice.entity.FdBatchRunLog;
import com.banklab.fdservice.service.BusinessClockService;

import lombok.extern.slf4j.Slf4j;

/**
 * Runs a batch job for a business date and records it in FD_BATCH_RUN_LOG. Shared by
 * the manual trigger endpoint and the scheduled end-of-day run.
 *
 * Runs execute synchronously and one at a time. Every run gets a unique run id job
 * parameter, so Spring Batch never refuses a re-run of the same date; idempotency
 * comes from the per-account guard dates instead, which makes a duplicate trigger a
 * run that reads 0 accounts (OpenAPI triggerBatchRun: "a no-op, not an error").
 */
@Slf4j
@Service
public class BatchJobService {

    static final String RUN_ID_PARAM = "runId";

    private final JobOperator jobOperator;
    private final Job accrualJob;
    private final Job maturityJob;
    private final BatchRunLogService runLogService;
    private final BatchSkipRecorder skipRecorder;
    private final BusinessClockService businessClock;
    private final ReentrantLock runLock = new ReentrantLock();

    public BatchJobService(JobOperator jobOperator,
            @Qualifier(InterestBatchJobConfig.ACCRUAL_JOB) Job accrualJob,
            @Qualifier(InterestBatchJobConfig.MATURITY_JOB) Job maturityJob,
            BatchRunLogService runLogService,
            BatchSkipRecorder skipRecorder,
            BusinessClockService businessClock) {
        this.jobOperator = jobOperator;
        this.accrualJob = accrualJob;
        this.maturityJob = maturityJob;
        this.runLogService = runLogService;
        this.skipRecorder = skipRecorder;
        this.businessClock = businessClock;
    }

    /**
     * Runs a job for {@code businessDate}, which may be the clock's date or earlier (a
     * re-run) but never later — see {@link BusinessDateAfterClockException}.
     */
    public BatchRun run(BatchJobName jobName, LocalDate businessDate) {
        LocalDate clockDate = businessClock.currentBusinessDate();
        if (businessDate.isAfter(clockDate)) {
            throw new BusinessDateAfterClockException(businessDate, clockDate);
        }
        if (!runLock.tryLock()) {
            throw new BatchJobAlreadyRunningException(
                    "Another batch run is in progress; retry " + jobName + " once it finishes");
        }
        try {
            FdBatchRunLog runLog = runLogService.start(jobName, businessDate);
            log.info("Batch {} started for business date {} (run {})", jobName, businessDate, runLog.getFdjbId());
            try {
                JobParameters parameters = new JobParametersBuilder()
                        .addString(InterestBatchJobConfig.BUSINESS_DATE_PARAM, businessDate.toString())
                        .addLong(RUN_ID_PARAM, runLog.getFdjbId())
                        .toJobParameters();
                JobExecution execution = jobOperator.start(jobFor(jobName), parameters);

                long read = sum(execution, StepExecution::getReadCount);
                long write = sum(execution, StepExecution::getWriteCount);
                long skip = sum(execution, StepExecution::getSkipCount);
                BatchRunStatus status = resolveStatus(execution.getStatus(), skip);
                String error = switch (status) {
                    case FAILED -> failureMessage(execution);
                    case PARTIAL -> skipRecorder.summary();
                    default -> null;
                };
                log.info("Batch {} for {} finished {}: read={}, write={}, skip={}", jobName, businessDate, status,
                        read, write, skip);
                return runLogService.finish(runLog.getFdjbId(), status, read, write, skip, error);
            } catch (Exception e) {
                log.error("Batch {} for {} could not run", jobName, businessDate, e);
                return runLogService.finish(runLog.getFdjbId(), BatchRunStatus.FAILED, 0, 0, 0, e.toString());
            }
        } finally {
            runLock.unlock();
        }
    }

    public List<BatchRun> listRuns(BatchJobName jobName, LocalDate businessDate) {
        return runLogService.list(jobName, businessDate);
    }

    /**
     * COMPLETED with no skips → SUCCESS; COMPLETED with skips → PARTIAL; anything else
     * (FAILED, STOPPED, ABANDONED, …) → FAILED.
     */
    static BatchRunStatus resolveStatus(BatchStatus batchStatus, long skipCount) {
        if (batchStatus != BatchStatus.COMPLETED) {
            return BatchRunStatus.FAILED;
        }
        return skipCount > 0 ? BatchRunStatus.PARTIAL : BatchRunStatus.SUCCESS;
    }

    private Job jobFor(BatchJobName jobName) {
        return switch (jobName) {
            case ACCRUAL -> accrualJob;
            case MATURITY -> maturityJob;
        };
    }

    private static long sum(JobExecution execution, ToLongFunction<StepExecution> counter) {
        return execution.getStepExecutions().stream().mapToLong(counter).sum();
    }

    private static String failureMessage(JobExecution execution) {
        return execution.getAllFailureExceptions().stream()
                .findFirst()
                .map(Throwable::toString)
                .orElse("Job ended with status " + execution.getStatus());
    }
}
