package com.banklab.fdservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * FD_BUSINESS_CLOCK (FDBC) — the controllable "today" for fd-service (ERD P4 /
 * §13). Batch jobs and account booking read the business date from here, never
 * from LocalDate.now(), so the date can be moved for testing and demos.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "FD_BUSINESS_CLOCK")
public class FdBusinessClock extends FdAuditableEntity {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "FDBC_ENTITY_CD", length = 10)
    private String entityCd;

    @Column(name = "FDBC_BUSINESS_DT", nullable = false)
    private LocalDate businessDt;

    @Column(name = "FDBC_PREV_BUSINESS_DT")
    private LocalDate prevBusinessDt;

    @Column(name = "FDBC_EOD_STS", length = 20)
    private String eodSts;

    @Column(name = "FDBC_LAST_EOD_TS")
    private LocalDateTime lastEodTs;

    @Column(name = "FDBC_EFCTV_DT")
    private LocalDate efctvDt;
}
