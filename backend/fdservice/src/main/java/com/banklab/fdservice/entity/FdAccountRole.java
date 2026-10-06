package com.banklab.fdservice.entity;

import java.time.LocalDate;
import java.util.UUID;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * FD_ACCOUNT_ROLES (FDRL) — owner / joint holder / nominee / guardian / guarantor /
 * beneficiary on an account. CUST_ID and FDRL_PRD_ROLE_ID are logical references only.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "FD_ACCOUNT_ROLES")
public class FdAccountRole extends FdAuditableEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FDRL_ID")
    private Long fdrlId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "FDA_ID", columnDefinition = "CHAR(36)", nullable = false)
    private UUID fdaId;

    @Column(name = "CUST_ID", length = 32, nullable = false)
    private String custId;

    @Column(name = "FDRL_PRD_ROLE_ID", length = 20)
    private String prdRoleId;

    @Column(name = "FDRL_ROLE_TYP", length = 30, nullable = false)
    private String roleTyp;

    @Column(name = "FDRL_IS_PRIMARY")
    private Boolean isPrimary;

    @Column(name = "FDRL_EFCTV_DT")
    private LocalDate efctvDt;
}
