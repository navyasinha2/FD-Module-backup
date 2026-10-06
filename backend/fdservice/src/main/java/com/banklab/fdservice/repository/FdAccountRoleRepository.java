package com.banklab.fdservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banklab.fdservice.entity.FdAccountRole;

public interface FdAccountRoleRepository extends JpaRepository<FdAccountRole, Long> {

    List<FdAccountRole> findByFdaId(UUID fdaId);
}
