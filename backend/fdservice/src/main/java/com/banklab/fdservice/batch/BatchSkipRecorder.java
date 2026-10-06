package com.banklab.fdservice.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Logs every account the batch skips and keeps the reasons for the run's
 * FDJB_ERROR_MSG. Only the FDA_ID is logged, never customer data. Runs are
 * serialized by {@link BatchJobService}, so one shared instance is enough; it resets
 * at the start of each step.
 */
@Slf4j
@Component
public class BatchSkipRecorder implements SkipListener<UUID, UUID>, StepExecutionListener {

    private final List<String> skipped = new ArrayList<>();
    private String stepName = "";

    @Override
    public synchronized void beforeStep(StepExecution stepExecution) {
        skipped.clear();
        stepName = stepExecution.getStepName();
    }

    @Override
    public synchronized void onSkipInWrite(UUID fdaId, Throwable cause) {
        log.warn("{} skipped account {}: {}", stepName, fdaId, cause.toString());
        skipped.add(fdaId + ": " + rootMessage(cause));
    }

    @Override
    public synchronized void onSkipInRead(Throwable cause) {
        log.warn("{} skipped a read: {}", stepName, cause.toString());
        skipped.add("read: " + rootMessage(cause));
    }

    /** e.g. "2 account(s) skipped — 1f0c…: Contracted interest rate (FDA_INT_RT) is not set; …" */
    public synchronized String summary() {
        if (skipped.isEmpty()) {
            return null;
        }
        return skipped.size() + " account(s) skipped - " + String.join("; ", skipped);
    }

    private static String rootMessage(Throwable cause) {
        Throwable root = cause;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }
}
