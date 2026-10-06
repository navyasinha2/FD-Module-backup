package com.banklab.fdservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banklab.fdservice.entity.FdGlAccount;

import jakarta.persistence.LockModeType;

public interface FdGlAccountRepository extends JpaRepository<FdGlAccount, String> {

    /**
     * Row-locks a GL account for the caller's transaction. FDGL_CURRENT_BAL is a hot
     * running total touched by every posting, so it follows the same pessimistic
     * pattern as FdAccountSequenceRepository#lockByBranchCd.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from FdGlAccount g where g.fdglCd = :fdglCd")
    Optional<FdGlAccount> lockByCode(@Param("fdglCd") String fdglCd);

    List<FdGlAccount> findAllByOrderByFdglCdAsc();
}
