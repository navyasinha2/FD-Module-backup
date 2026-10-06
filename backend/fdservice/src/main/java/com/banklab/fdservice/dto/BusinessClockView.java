package com.banklab.fdservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FD_BUSINESS_CLOCK as returned by /business-clock.
 */
public record BusinessClockView(
        String entityCd,
        LocalDate businessDate,
        LocalDate previousBusinessDate,
        EodStatus eodStatus,
        LocalDateTime lastEodTs) {
}
