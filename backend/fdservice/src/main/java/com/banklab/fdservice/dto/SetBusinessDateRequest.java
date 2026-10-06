package com.banklab.fdservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of PUT /business-clock.
 */
public record SetBusinessDateRequest(@NotNull LocalDate businessDate) {
}
