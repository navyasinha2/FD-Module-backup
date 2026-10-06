package com.banklab.fdservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * FD_BATCH_RUN_LOG (FDJB) — one summary row per batch job run (ERD §12). No retry
 * counter column: retries happen inside the Spring Batch step and are not persisted.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "FD_BATCH_RUN_LOG")
public class FdBatchRunLog extends FdAuditableEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FDJB_ID")
    private Long fdjbId;

    @Column(name = "FDJB_JOB_NAME", length = 50, nullable = false)
    private String jobName;

    @Column(name = "FDJB_BUSINESS_DT", nullable = false)
    private LocalDate businessDt;

    @Column(name = "FDJB_STS", length = 20, nullable = false)
    private String sts;

    @Column(name = "FDJB_READ_CNT")
    private Integer readCnt;

    @Column(name = "FDJB_WRITE_CNT")
    private Integer writeCnt;

    @Column(name = "FDJB_SKIP_CNT")
    private Integer skipCnt;

    @Column(name = "FDJB_START_TS")
    private LocalDateTime startTs;

    @Column(name = "FDJB_END_TS")
    private LocalDateTime endTs;

    @Column(name = "FDJB_ERROR_MSG", length = 500)
    private String errorMsg;

    @Column(name = "FDJB_EFCTV_DT")
    private LocalDate efctvDt;
}
