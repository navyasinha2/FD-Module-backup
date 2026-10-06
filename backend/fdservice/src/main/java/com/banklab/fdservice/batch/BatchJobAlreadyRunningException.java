package com.banklab.fdservice.batch;

/**
 * A batch run was requested while another run is in progress. Runs are serialized
 * (the job repository is in-memory and the jobs share accounts), so the second
 * caller gets a 409 instead of queueing silently.
 */
public class BatchJobAlreadyRunningException extends RuntimeException {

    public BatchJobAlreadyRunningException(String message) {
        super(message);
    }
}
