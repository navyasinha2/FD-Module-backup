package com.banklab.fdservice.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.banklab.fdservice.dto.BatchJobName;
import com.banklab.fdservice.dto.BatchRun;
import com.banklab.fdservice.dto.BatchRunStatus;
import com.banklab.fdservice.entity.FdBatchRunLog;
import com.banklab.fdservice.repository.FdBatchRunLogRepository;
import com.banklab.fdservice.service.FdAccountMapper;
import com.banklab.fdservice.service.FdLedgerService;

import lombok.RequiredArgsConstructor;

/**
 * FD_BATCH_RUN_LOG writes. Each write commits on its own (REQUIRES_NEW), so the
 * RUNNING row is visible while the job runs and a FAILED row survives the job's
 * own rollback.
 */
@Service
@RequiredArgsConstructor
public class BatchRunLogService {

    private static final int ERROR_MSG_MAX = 500;

    private final FdBatchRunLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FdBatchRunLog start(BatchJobName jobName, LocalDate businessDate) {
        FdBatchRunLog runLog = FdBatchRunLog.builder()
                .jobName(jobName.name())
                .businessDt(businessDate)
                .sts(BatchRunStatus.RUNNING.name())
                .readCnt(0)
                .writeCnt(0)
                .skipCnt(0)
                .startTs(LocalDateTime.now())
                .efctvDt(businessDate)
                .build();
        runLog.stampAudit(FdLedgerService.BATCH_USER, "C");
        return repository.save(runLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchRun finish(Long fdjbId, BatchRunStatus status, long read, long write, long skip, String errorMsg) {
        FdBatchRunLog runLog = repository.findById(fdjbId).orElseThrow();
        runLog.setSts(status.name());
        runLog.setReadCnt(Math.toIntExact(read));
        runLog.setWriteCnt(Math.toIntExact(write));
        runLog.setSkipCnt(Math.toIntExact(skip));
        runLog.setEndTs(LocalDateTime.now());
        runLog.setErrorMsg(truncate(errorMsg));
        runLog.stampAudit(FdLedgerService.BATCH_USER, "U");
        return FdAccountMapper.toResponse(runLog);
    }

    @Transactional(readOnly = true)
    public List<BatchRun> list(BatchJobName jobName, LocalDate businessDate) {
        List<FdBatchRunLog> rows = businessDate == null
                ? repository.findTop50ByJobNameOrderByFdjbIdDesc(jobName.name())
                : repository.findByJobNameAndBusinessDtOrderByFdjbIdDesc(jobName.name(), businessDate);
        return rows.stream().map(FdAccountMapper::toResponse).toList();
    }

    private static String truncate(String message) {
        if (message == null || message.length() <= ERROR_MSG_MAX) {
            return message;
        }
        return message.substring(0, ERROR_MSG_MAX - 3) + "...";
    }
}
