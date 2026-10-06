package com.banklab.fdservice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banklab.fdservice.entity.FdAccount;

import jakarta.persistence.LockModeType;

public interface FdAccountRepository extends JpaRepository<FdAccount, UUID> {

    Optional<FdAccount> findByAcctNum(String acctNum);

    /**
     * Row-locks one account for the caller's transaction, so a scheduled batch run, a
     * manual batch trigger and a premature withdrawal can never post against the same
     * account at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from FdAccount a where a.fdaId = :fdaId")
    Optional<FdAccount> lockById(@Param("fdaId") UUID fdaId);

    /**
     * ACCRUAL job reader: ACTIVE accounts that earn interest for this business date —
     * the deposit is already placed (value date on or before it), the date is before
     * maturity (the maturity day earns nothing; the MATURITY job closes those), and the
     * date hasn't been accrued yet.
     */
    @Query("""
            select a.fdaId from FdAccount a
            where a.sts = 'ACTIVE'
              and a.valueDt <= :businessDt
              and a.matDt > :businessDt
              and (a.lastAccrualDt is null or a.lastAccrualDt < :businessDt)
            order by a.acctNum
            """)
    List<UUID> findIdsDueForAccrual(@Param("businessDt") LocalDate businessDt);

    List<FdAccount> findByStsAndAccruedIntAmtGreaterThan(String sts, java.math.BigDecimal accruedIntAmt);

    /** MATURITY job reader. FDA_MAT_DT is only read here, never recalculated (ERD §4). */
    @Query("""
            select a.fdaId from FdAccount a
            where a.sts = 'ACTIVE' and a.matDt <= :businessDt
            order by a.acctNum
            """)
    List<UUID> findIdsDueForMaturity(@Param("businessDt") LocalDate businessDt);

    @Query("""
            select a from FdAccount a
            where (:custId is null or a.custId = :custId)
              and (:acctNum is null or a.acctNum = :acctNum)
              and (:sts is null or a.sts = :sts)
              and (:prdCode is null or a.prdCode = :prdCode)
            order by a.openDt desc, a.acctNum desc
            """)
    Page<FdAccount> search(@Param("custId") String custId, @Param("acctNum") String acctNum,
            @Param("sts") String sts, @Param("prdCode") String prdCode, Pageable pageable);
}
