package com.banklab.fdservice.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banklab.fdservice.dto.AccountStatus;
import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdAccountSequence;
import com.banklab.fdservice.repository.FdAccountRepository;
import com.banklab.fdservice.repository.FdAccountSequenceRepository;
import com.banklab.fdservice.service.BusinessClockService;
import com.banklab.fdservice.service.CurrencyDecimals;
import com.banklab.fdservice.service.FdLedgerService;
import com.banklab.fdservice.service.GlAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the reference rows a fresh fd_db needs before the first account can be
 * opened and batched: the GL account registry, the business clock, and the
 * FD_ACCOUNT_SEQUENCE row for the default branch. Existing rows are left untouched.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReferenceDataInitializer {

    private final GlAccountService glAccountService;
    private final BusinessClockService businessClockService;
    private final FdAccountSequenceRepository sequenceRepository;
    private final FdAccountRepository accountRepository;
    private final FdLedgerService ledgerService;

    @Value("${fd.account-number.default-branch-cd}")
    private String defaultBranchCd;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void seedReferenceData() {
        boolean introducingInterestPayable = !glAccountService.exists(glAccountService.interestPayableCd());
        glAccountService.ensureDefaults();
        LocalDate businessDate = businessClockService.currentBusinessDate();
        if (introducingInterestPayable) {
            recogniseOpeningAccruals(businessDate);
        }
        if (!sequenceRepository.existsById(defaultBranchCd)) {
            FdAccountSequence sequence = FdAccountSequence.builder()
                    .branchCd(defaultBranchCd)
                    .branchName("Head Office")
                    .prefix("FD")
                    .lastSeq(0L)
                    .seqWidth(6)
                    .efctvDt(LocalDate.now())
                    .build();
            sequence.stampAudit(FdLedgerService.SYSTEM_USER, "C");
            sequenceRepository.save(sequence);
        }
    }

    /**
     * One-time switch to daily GL accrual. Interest accrued on ACTIVE accounts before
     * FD_INT_PAYABLE existed was never recognised in the GL, so it is booked as an
     * opening accrual (Dr interest expense / Cr interest payable) when that GL account is
     * first created. Without this, the next capitalization would draw more out of
     * interest payable than was ever put in.
     */
    private void recogniseOpeningAccruals(LocalDate businessDate) {
        for (FdAccount account : accountRepository.findByStsAndAccruedIntAmtGreaterThan(
                AccountStatus.ACTIVE.name(), BigDecimal.ZERO)) {
            int decimals = account.getCcyDecimals() != null
                    ? account.getCcyDecimals()
                    : CurrencyDecimals.forCurrency(account.getCcyCd());
            BigDecimal amount = account.getAccruedIntAmt().setScale(decimals, RoundingMode.HALF_UP);
            LocalDate accruedThrough = account.getLastAccrualDt() != null ? account.getLastAccrualDt() : businessDate;
            ledgerService.postInterestAccrual(account, amount, accruedThrough, businessDate, FdLedgerService.SYSTEM_USER);
            log.info("Opening interest accrual of {} recognised for account {}", amount, account.getAcctNum());
        }
    }
}
