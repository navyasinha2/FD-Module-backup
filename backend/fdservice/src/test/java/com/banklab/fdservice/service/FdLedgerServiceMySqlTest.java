package com.banklab.fdservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdGlEntry;
import com.banklab.fdservice.entity.FdTransaction;
import com.banklab.fdservice.repository.FdAccountRepository;
import com.banklab.fdservice.repository.FdGlEntryRepository;
import com.banklab.fdservice.repository.FdTransactionRepository;
import com.banklab.fdservice.support.FdTestDatabase;

/**
 * Database tests for the shared ledger writer against MySQL: every posting type writes
 * the right FD_TRANSACTIONS row, a balanced pair of FD_GL_ENTRIES and the matching
 * FDGL_CURRENT_BAL changes — and a rejected posting writes nothing.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FdLedgerServiceMySqlTest {

    private static final LocalDate DAY = LocalDate.parse("2026-01-01");

    @Autowired
    private FdLedgerService ledger;

    @Autowired
    private GlAccountService glAccounts;

    @Autowired
    private FdAccountRepository accountRepository;

    @Autowired
    private FdTransactionRepository transactionRepository;

    @Autowired
    private FdGlEntryRepository glEntryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID fdaId;

    @BeforeEach
    void setUp() {
        new FdTestDatabase(jdbc).clearFdData();
        glAccounts.ensureDefaults();
        fdaId = accountRepository.saveAndFlush(account()).getFdaId();
    }

    @AfterEach
    void tearDown() {
        new FdTestDatabase(jdbc).clearFdData();
    }

    @Test
    void everyPostingTypeWritesBalancedGlLegsAndMovesTheRightBalances() {
        transactionTemplate.executeWithoutResult(status -> {
            FdAccount account = accountRepository.lockById(fdaId).orElseThrow();
            ledger.postDeposit(account, DAY, UUID.randomUUID(), "TEST");
            ledger.postInterestAccrual(account, new BigDecimal("500.00"), LocalDate.parse("2026-04-01"),
                    LocalDate.parse("2026-03-31"), "TEST");
            ledger.postInterestCapitalization(account, new BigDecimal("500.00"), LocalDate.parse("2026-04-01"),
                    LocalDate.parse("2026-04-01"), UUID.randomUUID(), "TEST");
            ledger.postInterestAccrual(account, new BigDecimal("100.00"), LocalDate.parse("2026-05-01"),
                    LocalDate.parse("2026-04-30"), "TEST");
            ledger.postInterestPayout(account, new BigDecimal("100.00"), LocalDate.parse("2026-05-01"),
                    LocalDate.parse("2026-05-01"), UUID.randomUUID(), "TEST");
            UUID closure = UUID.randomUUID();
            ledger.postPenalty(account, new BigDecimal("50.00"), LocalDate.parse("2026-06-01"), closure, "TEST");
            ledger.postWithdrawal(account, account.getPrincipalBal(), LocalDate.parse("2026-06-01"), closure, "TEST");
        });

        List<FdTransaction> txns = transactionRepository.findByFdaIdOrderByFdtIdAsc(fdaId);
        assertThat(txns).extracting(FdTransaction::getTxnTyp)
                .containsExactly("DEPOSIT", "INTEREST", "INTEREST", "PENALTY", "WITHDRAWAL");
        assertThat(txns).extracting(FdTransaction::getDrCr).containsExactly("C", "C", "C", "D", "D");
        assertBalances(txns.get(0), "0.00", "100000.00");
        assertBalances(txns.get(1), "100000.00", "100500.00");
        assertBalances(txns.get(2), "100500.00", "100500.00");
        assertBalances(txns.get(3), "100500.00", "100450.00");
        assertBalances(txns.get(4), "100450.00", "0.00");
        assertThat(txns.get(4).getAmt()).isEqualByComparingTo("100450.00");
        assertThat(txns.get(3).getTxnGrpId()).isEqualTo(txns.get(4).getTxnGrpId());
        assertThat(txns).allSatisfy(txn -> assertThat(txn.getUserId()).isEqualTo("TEST"));

        assertEveryGroupBalances();

        assertThat(accountRepository.findById(fdaId).orElseThrow().getPrincipalBal()).isEqualByComparingTo("0.00");
        Map<String, BigDecimal> gl = glBalances();
        assertThat(gl.get("FD_DEP_LIAB")).isEqualByComparingTo("0.00");
        assertThat(gl.get("FD_INT_EXP")).isEqualByComparingTo("600.00");
        assertThat(gl.get("FD_SETTLEMENT")).isEqualByComparingTo("-550.00");
        assertThat(gl.get("FD_PENALTY_INC")).isEqualByComparingTo("50.00");
        // Everything accrued was credited or paid out
        assertThat(gl.get("FD_INT_PAYABLE")).isEqualByComparingTo("0.00");
        // Accounting equation: assets + expenses == liabilities + income
        assertThat(gl.get("FD_SETTLEMENT").add(gl.get("FD_INT_EXP")))
                .isEqualByComparingTo(gl.get("FD_DEP_LIAB").add(gl.get("FD_INT_PAYABLE")).add(gl.get("FD_PENALTY_INC")));
        // Accruals are GL-only: the two accrual groups have no FD_TRANSACTIONS row
        assertThat(glEntryRepository.findAll()).filteredOn(entry -> entry.getFdtId() == null).hasSize(4);
    }

    @Test
    void negativeAccrualReversesRecognisedInterestWithoutACustomerTransaction() {
        transactionTemplate.executeWithoutResult(status -> {
            FdAccount account = accountRepository.lockById(fdaId).orElseThrow();
            ledger.postInterestAccrual(account, new BigDecimal("40.00"), DAY, DAY, "TEST");
            ledger.postInterestAccrual(account, new BigDecimal("-15.00"), DAY, DAY, "TEST");
        });

        assertThat(transactionRepository.findByFdaIdOrderByFdtIdAsc(fdaId)).isEmpty();
        assertThat(glEntryRepository.findAll()).hasSize(4).allSatisfy(entry -> assertThat(entry.getFdtId()).isNull());
        assertThat(glBalances().get("FD_INT_PAYABLE")).isEqualByComparingTo("25.00");
        assertThat(glBalances().get("FD_INT_EXP")).isEqualByComparingTo("25.00");
        assertEveryGroupBalances();
    }

    @Test
    void nonPositiveAmountIsRejectedAndNothingIsWritten() {
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            FdAccount account = accountRepository.lockById(fdaId).orElseThrow();
            ledger.postInterestCapitalization(account, BigDecimal.ZERO, DAY, DAY, UUID.randomUUID(), "TEST");
        })).isInstanceOf(IllegalArgumentException.class);

        assertThat(transactionRepository.findByFdaIdOrderByFdtIdAsc(fdaId)).isEmpty();
        assertThat(glEntryRepository.count()).isZero();
    }

    @Test
    void debitLargerThanTheBalanceIsRejectedAndRolledBack() {
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            FdAccount account = accountRepository.lockById(fdaId).orElseThrow();
            ledger.postDeposit(account, DAY, UUID.randomUUID(), "TEST");
            ledger.postWithdrawal(account, new BigDecimal("100000.01"), DAY, UUID.randomUUID(), "TEST");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(transactionRepository.findByFdaIdOrderByFdtIdAsc(fdaId)).isEmpty();
        assertThat(glBalances().get("FD_DEP_LIAB")).isEqualByComparingTo("0.00");
    }

    private void assertEveryGroupBalances() {
        Map<UUID, List<FdGlEntry>> groups = glEntryRepository.findAll().stream()
                .collect(Collectors.groupingBy(FdGlEntry::getTxnGrpId));
        assertThat(groups).isNotEmpty();
        groups.forEach((group, legs) -> {
            BigDecimal debits = sum(legs, "D");
            BigDecimal credits = sum(legs, "C");
            assertThat(debits).as("debits == credits for group %s", group).isEqualByComparingTo(credits);
        });
    }

    private static BigDecimal sum(List<FdGlEntry> legs, String side) {
        return legs.stream().filter(leg -> side.equals(leg.getDrCr())).map(FdGlEntry::getAmt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> glBalances() {
        return jdbc.query("SELECT FDGL_CD, FDGL_CURRENT_BAL FROM FD_GL_ACCOUNTS", rs -> {
            Map<String, BigDecimal> balances = new java.util.HashMap<>();
            while (rs.next()) {
                balances.put(rs.getString(1), rs.getBigDecimal(2));
            }
            return balances;
        });
    }

    private static void assertBalances(FdTransaction txn, String before, String after) {
        assertThat(txn.getBalBefore()).as("%s balance before", txn.getTxnTyp()).isEqualByComparingTo(before);
        assertThat(txn.getBalAfter()).as("%s balance after", txn.getTxnTyp()).isEqualByComparingTo(after);
    }

    private static FdAccount account() {
        FdAccount account = FdAccount.builder()
                .fdaId(UUID.randomUUID())
                .acctNum("FD001800001")
                .custId("CUST-002")
                .prdCode("FD-REG")
                .ccyCd("INR")
                .ccyDecimals(2)
                .principalAmt(new BigDecimal("100000.00"))
                .principalBal(new BigDecimal("100000.00"))
                .accruedIntAmt(BigDecimal.ZERO)
                .intRt(new BigDecimal("6.5"))
                .intTyp("COMPOUND")
                .compoundFreq("QUARTERLY")
                .dayCountConv("ACT/365")
                .tenureMonths(12)
                .openDt(DAY)
                .valueDt(DAY)
                .matDt(DAY.plusMonths(12))
                .sts("ACTIVE")
                .build();
        account.stampAudit("TEST", "C");
        return account;
    }
}
