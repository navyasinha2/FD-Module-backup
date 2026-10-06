package com.banklab.fdservice.dto;

/**
 * FDBC_EOD_STS — where the end-of-day cycle for the current business date stands.
 */
public enum EodStatus {
    OPEN,
    RUNNING,
    COMPLETED,
    FAILED
}
