package com.banklab.fdservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdBatchRunLog;
import com.banklab.fdservice.entity.FdBusinessClock;
import com.banklab.fdservice.entity.FdGlAccount;
import com.banklab.fdservice.entity.FdGlEntry;
import com.banklab.fdservice.entity.FdTransaction;

/**
 * Database tests: CRUD and constraints for the FD tables against real MySQL
 * (fd_db_test, schema generated from the entities). Each test runs in a transaction
 * that is rolled back afterwards.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FdRepositoryMySqlTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FdAccountRepository accountRepository;

    @Autowired
    private FdTransactionRepository transactionRepository;

    @Autowired
    private FdGlAccountRepository glAccountRepository;

    @Autowired
    private FdGlEntryRepository glEntryRepository;

    @Autowired
    private FdBusinessClockRepository clockRepository;

    @Autowired
    private FdBatchRunLogRepository batchRunLogRepository;

    @BeforeEach
    void cleanTables() {
        jdbc.update("DELETE FROM FD_GL_ENTRIES");
        jdbc.update("DELETE FROM FD_TRANSACTIONS");
        jdbc.update("DELETE FROM FD_ACCOUNT_ROLES");
        jdbc.update("DELETE FROM FD_ACCOUNTS");
        jdbc.update("DELETE FROM FD_BATCH_RUN_LOG");
    }

    @Test
    void testsRunAgainstMySql() {
        assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class)).isEqualTo("fd_db_test");
        assertThat(jdbc.queryForObject("SELECT VERSION()", String.class)).startsWith("8.");
    }

    @Test
    void fdAccountCreateReadUpdateDelete() {
        FdAccount saved = accountRepository.saveAndFlush(sampleAccount("FD001900001", "2026-01-01", 12));

        FdAccount read = accountRepository.findByAcctNum("FD001900001").orElseThrow();
        assertThat(read.getFdaId()).isEqualTo(saved.getFdaId());
        assertThat(read.getPrincipalAmt()).isEqualByComparingTo("100000.00");
        assertThat(read.getIntRt()).isEqualByComparingTo("6.5000");
        assertThat(read.getMatDt()).isEqualTo(LocalDate.parse("2027-01-01"));
        assertThat(read.getUuid()).isNotBlank();

        read.setAccruedIntAmt(new BigDecimal("801.3699"));
        read.setLastAccrualDt(LocalDate.parse("2026-02-15"));
        accountRepository.saveAndFlush(read);
        assertThat(jdbc.queryForObject("SELECT FDA_ACCRUED_INT_AMT FROM FD_ACCOUNTS WHERE FDA_ACCT_NUM = ?",
                BigDecimal.class, "FD001900001")).isEqualByComparingTo("801.3699");

        accountRepository.delete(read);
        accountRepository.flush();
        assertThat(accountRepository.findByAcctNum("FD001900001")).isEmpty();
    }

    @Test
    void accountNumberIsUnique() {
        accountRepository.saveAndFlush(sampleAccount("FD001900002", "2026-01-01", 12));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(sampleAccount("FD001900002", "2026-01-01", 6)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void transactionForANonExistentAccountViolatesTheForeignKey() {
        assertThatThrownBy(() -> transactionRepository.saveAndFlush(sampleTransaction(UUID.randomUUID(), "DEPOSIT",
                "C", "1000.00", "0.00", "1000.00", "2026-01-01")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void glEntryForAnUnknownGlCodeViolatesTheForeignKey() {
        FdAccount account = accountRepository.saveAndFlush(sampleAccount("FD001900003", "2026-01-01", 12));
        FdTransaction txn = transactionRepository.saveAndFlush(sampleTransaction(account.getFdaId(), "DEPOSIT", "C",
                "1000.00", "0.00", "1000.00", "2026-01-01"));

        assertThatThrownBy(() -> glEntryRepository.saveAndFlush(glEntry("NO_SUCH_GL", txn, "D")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void transactionsAndGlEntriesCrudWithSearchAndSums() {
        FdAccount account = accountRepository.saveAndFlush(sampleAccount("FD001900004", "2026-01-01", 12));
        UUID fdaId = account.getFdaId();
        glAccountRepository.saveAndFlush(glAccount("TST_LIAB", "LIABILITY"));
        glAccountRepository.saveAndFlush(glAccount("TST_ASSET", "ASSET"));

        FdTransaction deposit = transactionRepository.saveAndFlush(
                sampleTransaction(fdaId, "DEPOSIT", "C", "100000.00", "0.00", "100000.00", "2026-01-01"));
        FdTransaction interest1 = transactionRepository.saveAndFlush(
                sampleTransaction(fdaId, "INTEREST", "C", "1602.74", "100000.00", "101602.74", "2026-04-01"));
        transactionRepository.saveAndFlush(
                sampleTransaction(fdaId, "INTEREST", "C", "1646.52", "101602.74", "103249.26", "2026-07-01"));

        glEntryRepository.saveAndFlush(glEntry("TST_ASSET", deposit, "D"));
        glEntryRepository.saveAndFlush(glEntry("TST_LIAB", deposit, "C"));

        assertThat(transactionRepository.findByFdaIdOrderByFdtIdAsc(fdaId)).hasSize(3);
        assertThat(transactionRepository.sumAmountByType(fdaId, "INTEREST")).isEqualByComparingTo("3249.26");
        assertThat(transactionRepository.sumAmountByType(fdaId, "PENALTY")).isEqualByComparingTo("0");
        assertThat(transactionRepository.search(fdaId, "INTEREST", null, null, PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(2);
        assertThat(transactionRepository.search(fdaId, null, LocalDate.parse("2026-03-01"),
                LocalDate.parse("2026-05-01"), PageRequest.of(0, 10)).getContent())
                .extracting(FdTransaction::getFdtId).containsExactly(interest1.getFdtId());

        List<FdGlEntry> legs = glEntryRepository.findByTxnGrpId(deposit.getTxnGrpId());
        assertThat(legs).extracting(FdGlEntry::getDrCr).containsExactlyInAnyOrder("D", "C");
        assertThat(glEntryRepository.findByFdtId(deposit.getFdtId())).hasSize(2);

        interest1.setRemarks("corrected remark");
        transactionRepository.saveAndFlush(interest1);
        assertThat(transactionRepository.findById(interest1.getFdtId()).orElseThrow().getRemarks())
                .isEqualTo("corrected remark");

        glEntryRepository.deleteAll(legs);
        transactionRepository.delete(deposit);
        transactionRepository.flush();
        assertThat(transactionRepository.findByFdaIdOrderByFdtIdAsc(fdaId)).hasSize(2);
    }

    @Test
    void accrualReaderSelectsOnlyActiveAccountsNotYetAccruedForTheDate() {
        LocalDate businessDate = LocalDate.parse("2026-03-01");
        FdAccount due = accountRepository.saveAndFlush(sampleAccount("FD001900010", "2026-01-01", 12));

        FdAccount alreadyAccrued = sampleAccount("FD001900011", "2026-01-01", 12);
        alreadyAccrued.setLastAccrualDt(businessDate);
        accountRepository.saveAndFlush(alreadyAccrued);

        FdAccount closed = sampleAccount("FD001900012", "2026-01-01", 12);
        closed.setSts("CLOSED");
        accountRepository.saveAndFlush(closed);

        // Deposited on the business date itself: that day earns interest, so it is due
        FdAccount depositedToday = accountRepository.saveAndFlush(sampleAccount("FD001900013", "2026-03-01", 12));

        FdAccount accruedToMaturity = sampleAccount("FD001900014", "2026-01-01", 1);
        accruedToMaturity.setLastAccrualDt(LocalDate.parse("2026-01-31"));
        FdAccount matured = accountRepository.saveAndFlush(accruedToMaturity);

        accountRepository.saveAndFlush(sampleAccount("FD001900015", "2026-03-02", 12));

        assertThat(accountRepository.findIdsDueForAccrual(businessDate))
                .containsExactly(due.getFdaId(), depositedToday.getFdaId());
        assertThat(accountRepository.findIdsDueForMaturity(businessDate)).containsExactly(matured.getFdaId());
        assertThat(accountRepository.findIdsDueForMaturity(LocalDate.parse("2026-01-31"))).isEmpty();
    }

    @Test
    void lockByIdReturnsTheAccountUnderARowLock() {
        FdAccount saved = accountRepository.saveAndFlush(sampleAccount("FD001900020", "2026-01-01", 12));

        assertThat(accountRepository.lockById(saved.getFdaId())).isPresent();
        assertThat(accountRepository.lockById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void businessClockCreateReadUpdate() {
        clockRepository.saveAndFlush(FdBusinessClock.builder()
                .entityCd("TSTCLK")
                .businessDt(LocalDate.parse("2026-01-01"))
                .eodSts("OPEN")
                .build());

        FdBusinessClock clock = clockRepository.lockByEntityCd("TSTCLK").orElseThrow();
        clock.setPrevBusinessDt(clock.getBusinessDt());
        clock.setBusinessDt(LocalDate.parse("2026-01-02"));
        clockRepository.saveAndFlush(clock);

        FdBusinessClock read = clockRepository.findById("TSTCLK").orElseThrow();
        assertThat(read.getBusinessDt()).isEqualTo(LocalDate.parse("2026-01-02"));
        assertThat(read.getPrevBusinessDt()).isEqualTo(LocalDate.parse("2026-01-01"));

        clockRepository.delete(read);
        clockRepository.flush();
        assertThat(clockRepository.findById("TSTCLK")).isEmpty();
    }

    @Test
    void batchRunLogCreateReadUpdate() {
        FdBatchRunLog run = batchRunLogRepository.saveAndFlush(FdBatchRunLog.builder()
                .jobName("ACCRUAL")
                .businessDt(LocalDate.parse("2026-02-01"))
                .sts("RUNNING")
                .startTs(LocalDateTime.now())
                .build());

        run.setSts("PARTIAL");
        run.setReadCnt(2);
        run.setWriteCnt(1);
        run.setSkipCnt(1);
        run.setErrorMsg("1 account(s) skipped");
        batchRunLogRepository.saveAndFlush(run);

        assertThat(batchRunLogRepository.findByJobNameAndBusinessDtOrderByFdjbIdDesc("ACCRUAL",
                LocalDate.parse("2026-02-01"))).singleElement().satisfies(row -> {
                    assertThat(row.getSts()).isEqualTo("PARTIAL");
                    assertThat(row.getSkipCnt()).isEqualTo(1);
                });
        assertThat(batchRunLogRepository.findTop50ByJobNameOrderByFdjbIdDesc("MATURITY")).isEmpty();
    }

    private static FdAccount sampleAccount(String acctNum, String valueDate, int tenureMonths) {
        LocalDate value = LocalDate.parse(valueDate);
        FdAccount account = FdAccount.builder()
                .fdaId(UUID.randomUUID())
                .acctNum(acctNum)
                .custId("CUST-001")
                .prdCode("FD-REG")
                .rateId("RATE-12M")
                .ccyCd("INR")
                .ccyDecimals(2)
                .principalAmt(new BigDecimal("100000.00"))
                .principalBal(new BigDecimal("100000.00"))
                .accruedIntAmt(BigDecimal.ZERO)
                .intRt(new BigDecimal("6.5"))
                .intTyp("COMPOUND")
                .compoundFreq("QUARTERLY")
                .payoutFreq("ON_MATURITY")
                .dayCountConv("ACT/365")
                .tenureMonths(tenureMonths)
                .openDt(value)
                .valueDt(value)
                .matDt(value.plusMonths(tenureMonths))
                .matInstruction("PAYOUT")
                .sts("ACTIVE")
                .custNameSnap("Asha Rao")
                .prdNameSnap("Regular Fixed Deposit")
                .build();
        account.stampAudit("TEST", "C");
        return account;
    }

    private static FdTransaction sampleTransaction(UUID fdaId, String type, String drCr, String amount,
            String before, String after, String date) {
        return FdTransaction.builder()
                .fdaId(fdaId)
                .txnGrpId(UUID.randomUUID())
                .txnTyp(type)
                .drCr(drCr)
                .amt(new BigDecimal(amount))
                .balBefore(new BigDecimal(before))
                .balAfter(new BigDecimal(after))
                .ccyCd("INR")
                .txnDt(LocalDate.parse(date))
                .valueDt(LocalDate.parse(date))
                .txnTs(LocalDateTime.now())
                .build();
    }

    private static FdGlAccount glAccount(String code, String type) {
        return FdGlAccount.builder()
                .fdglCd(code)
                .name("Test " + code)
                .typ(type)
                .ccyCd("INR")
                .currentBal(BigDecimal.ZERO)
                .isActive(true)
                .build();
    }

    private static FdGlEntry glEntry(String glCd, FdTransaction txn, String drCr) {
        return FdGlEntry.builder()
                .glCd(glCd)
                .fdtId(txn.getFdtId())
                .txnGrpId(txn.getTxnGrpId())
                .drCr(drCr)
                .amt(txn.getAmt())
                .postDt(txn.getTxnDt())
                .narrative("test")
                .build();
    }
}
