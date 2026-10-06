package com.banklab.fdservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banklab.fdservice.entity.FdGlEntry;

public interface FdGlEntryRepository extends JpaRepository<FdGlEntry, Long> {

    List<FdGlEntry> findByTxnGrpId(UUID txnGrpId);

    List<FdGlEntry> findByFdtId(Long fdtId);
}
