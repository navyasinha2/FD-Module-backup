package com.banklab.fdservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banklab.fdservice.entity.FdBatchRunLog;

public interface FdBatchRunLogRepository extends JpaRepository<FdBatchRunLog, Long> {

    List<FdBatchRunLog> findTop50ByJobNameOrderByFdjbIdDesc(String jobName);

    List<FdBatchRunLog> findByJobNameAndBusinessDtOrderByFdjbIdDesc(String jobName, LocalDate businessDt);
}
