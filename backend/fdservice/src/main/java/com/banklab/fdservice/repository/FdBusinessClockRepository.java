package com.banklab.fdservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banklab.fdservice.entity.FdBusinessClock;

import jakarta.persistence.LockModeType;

public interface FdBusinessClockRepository extends JpaRepository<FdBusinessClock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from FdBusinessClock c where c.entityCd = :entityCd")
    Optional<FdBusinessClock> lockByEntityCd(@Param("entityCd") String entityCd);
}
