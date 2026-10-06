package com.banklab.fdservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of POST /fd-accounts/{accountId}/interest-calculations.
 */
public record InterestPreviewRequest(@NotNull LocalDate periodStart, @NotNull LocalDate periodEnd) {
}
