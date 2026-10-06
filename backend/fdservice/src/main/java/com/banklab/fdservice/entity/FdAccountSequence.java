package com.banklab.fdservice.entity;

import java.time.LocalDate;

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
 * FD_ACCOUNT_SEQUENCE (FDSQ) — one row per branch, holding the live counter that
 * generates the account number's per-branch sequence digits (ERD §3/§8). Not FK'd
 * from FD_ACCOUNTS; only used at generation time before the account row exists.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "FD_ACCOUNT_SEQUENCE")
public class FdAccountSequence extends FdAuditableEntity {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "FDSQ_BRANCH_CD", length = 10)
    private String branchCd;

    @Column(name = "FDSQ_BRANCH_NAME", length = 100)
    private String branchName;

    @Column(name = "FDSQ_PREFIX", length = 6)
    private String prefix;

    @Column(name = "FDSQ_LAST_SEQ", nullable = false)
    private Long lastSeq;

    @Column(name = "FDSQ_SEQ_WIDTH", nullable = false)
    private Integer seqWidth;

    @Column(name = "FDSQ_EFCTV_DT")
    private LocalDate efctvDt;
}
