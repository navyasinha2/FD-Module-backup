package com.banklab.fdservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body of POST /business-clock/advance.
 */
public record AdvanceBusinessDateRequest(@NotNull @Min(1) @Max(3660) Integer days) {
}
