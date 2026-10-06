package com.banklab.fdservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of POST /batch-jobs/{jobName}/runs.
 */
public record TriggerBatchRunRequest(@NotNull LocalDate businessDate) {
}
