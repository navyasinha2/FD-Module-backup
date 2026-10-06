package com.banklab.fdservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banklab.fdservice.entity.FdAccountSequence;

import jakarta.persistence.LockModeType;

public interface FdAccountSequenceRepository extends JpaRepository<FdAccountSequence, String> {

    /**
     * Row-locks the branch's sequence for the duration of the caller's transaction
     * (SELECT ... FOR UPDATE), so concurrent callers for the same branch serialize
     * instead of racing on FDSQ_LAST_SEQ.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from FdAccountSequence s where s.branchCd = :branchCd")
    Optional<FdAccountSequence> lockByBranchCd(@Param("branchCd") String branchCd);
}
