package com.banklab.fdservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Matches openapi schema BatchRun — one FD_BATCH_RUN_LOG row.
 */
public record BatchRun(
        Long fdjbId,
        BatchJobName jobName,
        LocalDate businessDt,
        BatchRunStatus status,
        Integer readCnt,
        Integer writeCnt,
        Integer skipCnt,
        LocalDateTime startTs,
        LocalDateTime endTs,
        String errorMsg) {
}
