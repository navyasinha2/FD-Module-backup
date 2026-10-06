package com.banklab.fdservice.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banklab.fdservice.entity.FdTransaction;

public interface FdTransactionRepository extends JpaRepository<FdTransaction, Long> {

    List<FdTransaction> findByFdaIdOrderByFdtIdAsc(UUID fdaId);

    @Query("""
            select t from FdTransaction t
            where t.fdaId = :fdaId
              and (:txnTyp is null or t.txnTyp = :txnTyp)
              and (:fromDt is null or t.txnDt >= :fromDt)
              and (:toDt is null or t.txnDt <= :toDt)
            order by t.fdtId
            """)
    Page<FdTransaction> search(@Param("fdaId") UUID fdaId, @Param("txnTyp") String txnTyp,
            @Param("fromDt") LocalDate fromDt, @Param("toDt") LocalDate toDt, Pageable pageable);

    @Query("select coalesce(sum(t.amt), 0) from FdTransaction t where t.fdaId = :fdaId and t.txnTyp = :txnTyp")
    BigDecimal sumAmountByType(@Param("fdaId") UUID fdaId, @Param("txnTyp") String txnTyp);
}
