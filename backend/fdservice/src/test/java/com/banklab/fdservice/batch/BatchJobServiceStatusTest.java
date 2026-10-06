package com.banklab.fdservice.batch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;

import com.banklab.fdservice.dto.BatchRunStatus;

/**
 * Unit test for how a Spring Batch outcome maps onto FDJB_STS.
 */
class BatchJobServiceStatusTest {

    @Test
    void completedWithoutSkipsIsSuccess() {
        assertThat(BatchJobService.resolveStatus(BatchStatus.COMPLETED, 0)).isEqualTo(BatchRunStatus.SUCCESS);
    }

    @Test
    void completedWithSkippedAccountsIsPartial() {
        assertThat(BatchJobService.resolveStatus(BatchStatus.COMPLETED, 2)).isEqualTo(BatchRunStatus.PARTIAL);
    }

    @Test
    void anythingButCompletedIsFailed() {
        assertThat(BatchJobService.resolveStatus(BatchStatus.FAILED, 0)).isEqualTo(BatchRunStatus.FAILED);
        assertThat(BatchJobService.resolveStatus(BatchStatus.STOPPED, 0)).isEqualTo(BatchRunStatus.FAILED);
        assertThat(BatchJobService.resolveStatus(BatchStatus.ABANDONED, 3)).isEqualTo(BatchRunStatus.FAILED);
    }
}
