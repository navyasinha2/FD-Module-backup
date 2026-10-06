package com.banklab.fdservice.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banklab.fdservice.batch.BatchJobService;
import com.banklab.fdservice.dto.BatchJobName;
import com.banklab.fdservice.dto.BatchRun;
import com.banklab.fdservice.dto.TriggerBatchRunRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * OpenAPI Batch tag (Action 17). The run executes synchronously; 202 is kept from the
 * contract and the body is the finished BatchRun. Role checks (ops/admin only) wait on
 * Group 1's JWT — SecurityConfig currently permits everything.
 */
@RestController
@RequestMapping("/batch-jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobService batchJobService;

    @PostMapping("/{jobName}/runs")
    public ResponseEntity<BatchRun> triggerBatchRun(@PathVariable BatchJobName jobName,
            @Valid @RequestBody TriggerBatchRunRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(batchJobService.run(jobName, request.businessDate()));
    }

    @GetMapping("/{jobName}/runs")
    public List<BatchRun> listBatchRuns(@PathVariable BatchJobName jobName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        return batchJobService.listRuns(jobName, businessDate);
    }
}
