package com.banklab.fdservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.banklab.fdservice.dto.EodStatus;
import com.banklab.fdservice.entity.FdBusinessClock;
import com.banklab.fdservice.repository.FdBusinessClockRepository;

import lombok.RequiredArgsConstructor;

/**
 * Reads and moves FD_BUSINESS_CLOCK. This is the only source of "today" for booking,
 * batch runs and withdrawals (ERD P4). The row is created on first use from
 * {@code fd.business-clock.seed-date}, or the system date if that's blank.
 */
@Service
@RequiredArgsConstructor
public class BusinessClockService {

    private final FdBusinessClockRepository repository;

    @Value("${fd.business-clock.entity-cd}")
    private String entityCd;

    @Value("${fd.business-clock.seed-date:}")
    private String seedDate;

    @Transactional
    public LocalDate currentBusinessDate() {
        return getOrSeed().getBusinessDt();
    }

    @Transactional
    public FdBusinessClock getClock() {
        return getOrSeed();
    }

    /**
     * Sets the business date directly ("time travel" for testing and demos). Moving
     * backwards is allowed on purpose; the batch guards make re-running an already
     * processed date a no-op.
     */
    @Transactional
    public FdBusinessClock setBusinessDate(LocalDate businessDate) {
        FdBusinessClock clock = lockOrSeed();
        clock.setPrevBusinessDt(clock.getBusinessDt());
        clock.setBusinessDt(businessDate);
        clock.setEodSts(EodStatus.OPEN.name());
        clock.setEfctvDt(businessDate);
        clock.stampAudit(FdLedgerService.API_USER, "U");
        return clock;
    }

    @Transactional
    public FdBusinessClock advance(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("days must be at least 1");
        }
        return setBusinessDate(lockOrSeed().getBusinessDt().plusDays(days));
    }

    @Transactional
    public FdBusinessClock markEod(EodStatus status) {
        FdBusinessClock clock = lockOrSeed();
        clock.setEodSts(status.name());
        if (status == EodStatus.COMPLETED || status == EodStatus.FAILED) {
            clock.setLastEodTs(LocalDateTime.now());
        }
        clock.stampAudit(FdLedgerService.BATCH_USER, "U");
        return clock;
    }

    private FdBusinessClock getOrSeed() {
        return repository.findById(entityCd).orElseGet(this::seed);
    }

    private FdBusinessClock lockOrSeed() {
        return repository.lockByEntityCd(entityCd).orElseGet(this::seed);
    }

    private FdBusinessClock seed() {
        LocalDate initial = StringUtils.hasText(seedDate) ? LocalDate.parse(seedDate.trim()) : LocalDate.now();
        FdBusinessClock clock = FdBusinessClock.builder()
                .entityCd(entityCd)
                .businessDt(initial)
                .eodSts(EodStatus.OPEN.name())
                .efctvDt(initial)
                .build();
        clock.stampAudit(FdLedgerService.SYSTEM_USER, "C");
        return repository.saveAndFlush(clock);
    }
}
