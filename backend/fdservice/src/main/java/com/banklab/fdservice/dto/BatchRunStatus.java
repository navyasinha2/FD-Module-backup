package com.banklab.fdservice.dto;

/**
 * FDJB_STS. Follows the OpenAPI BatchRun.status enum rather than the ERD doc's
 * RUNNING / COMPLETED / FAILED, because a chunked job with a skip policy needs a way
 * to say "finished, but some accounts were skipped" — that is PARTIAL.
 */
public enum BatchRunStatus {
    RUNNING,
    SUCCESS,
    FAILED,
    PARTIAL
}
