package com.banklab.fdservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.banklab.fdservice.batch.EndOfDayScheduler;
import com.banklab.fdservice.batch.EndOfDayService;
import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdBusinessClock;
import com.banklab.fdservice.entity.FdTransaction;
import com.banklab.fdservice.support.FdIntegrationTestSupport;

/**
 * End-to-end interest batch behaviour beyond the happy path: the payout write path,
 * skip handling, the scheduled EOD cycle with the business clock, and API validation.
 */
class InterestBatchIntegrationTest extends FdIntegrationTestSupport {

    @Autowired
    private EndOfDayService endOfDayService;

    @Test
    void simpleInterestWithMonthlyPayoutPaysInterestOutWithoutGrowingTheDeposit() throws Exception {
        UUID fdaId = createAccount(accountJson("120000.00", 3, "RATE-3M", "SIMPLE", null, "MONTHLY"));
        assertThat(account(fdaId).getMatAmt()).isEqualByComparingTo("120000.00");

        runBatch("ACCRUAL", "2026-03-01")
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.readCnt").value(1));

        List<FdTransaction> payouts = transactions(fdaId).subList(1, 3);
        assertThat(payouts).extracting(FdTransaction::getAmt)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("662.47"), new BigDecimal("598.36"));
        assertThat(payouts).allSatisfy(txn -> {
            assertThat(txn.getTxnTyp()).isEqualTo("INTEREST");
            assertThat(txn.getBalBefore()).isEqualByComparingTo("120000.00");
            assertThat(txn.getBalAfter()).isEqualByComparingTo("120000.00");
            assertThat(txn.getRemarks()).startsWith("Interest paid out");
        });
        FdAccount afterPayouts = account(fdaId);
        assertThat(afterPayouts.getPrincipalBal()).isEqualByComparingTo("120000.00");
        assertThat(afterPayouts.getLastCaptlzDt()).isEqualTo(date("2026-03-01"));
        // Payouts go to settlement, not the deposit liability
        assertThat(glBalances().get("FD_DEP_LIAB")).isEqualByComparingTo("120000.00");
        assertThat(glBalances().get("FD_SETTLEMENT")).isEqualByComparingTo("118739.17");

        runBatch("MATURITY", "2026-04-01").andExpect(jsonPath("$.readCnt").value(1));
        assertThat(transactions(fdaId)).extracting(FdTransaction::getTxnTyp)
                .containsExactly("DEPOSIT", "INTEREST", "INTEREST", "INTEREST", "MATURITY_PAYOUT");
        assertThat(transactions(fdaId).get(4).getAmt()).isEqualByComparingTo("120000.00");
        assertThat(account(fdaId).getSts()).isEqualTo("MATURED_CLOSED");

        assertLedgerReconciles();
        assertThat(glBalances().get("FD_DEP_LIAB")).isEqualByComparingTo("0.00");
        assertThat(glBalances().get("FD_INT_EXP")).isEqualByComparingTo("1923.30");
    }

    @Test
    void accountThatCannotBeCalculatedIsSkippedAndTheRestOfTheRunCompletes() throws Exception {
        UUID good = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        UUID noRate = createAccount(accountJson("50000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        removeContractedRate(noRate);

        runBatch("ACCRUAL", "2026-02-01")
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.readCnt").value(2))
                .andExpect(jsonPath("$.skipCnt").value(1))
                .andExpect(jsonPath("$.errorMsg", allOf(containsString(noRate.toString()), containsString("FDA_INT_RT"))));

        // 32 days at 6.5% on 1,00,000 — 1 Jan up to and including 1 Feb
        assertThat(account(good).getAccruedIntAmt()).isEqualByComparingTo("569.8630");
        assertThat(account(good).getLastAccrualDt()).isEqualTo(date("2026-02-01"));
        assertThat(account(noRate).getLastAccrualDt()).isNull();

        mockMvc.perform(get("/batch-jobs/ACCRUAL/runs").param("businessDate", "2026-02-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PARTIAL"))
                .andExpect(jsonPath("$[0].skipCnt").value(1));

        // The next run retries only the skipped account; the good one is already done
        runBatch("ACCRUAL", "2026-02-01")
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.readCnt").value(1))
                .andExpect(jsonPath("$.skipCnt").value(1));
        assertThat(account(good).getAccruedIntAmt()).isEqualByComparingTo("569.8630");
    }

    @Test
    void batchRunForADateAfterTheBusinessClockIsRejected() throws Exception {
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));

        mockMvc.perform(post("/batch-jobs/{jobName}/runs", "ACCRUAL").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessDate\":\"2026-06-01\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_DATE_AFTER_CLOCK"));
        mockMvc.perform(post("/batch-jobs/{jobName}/runs", "MATURITY").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessDate\":\"2027-01-01\"}"))
                .andExpect(status().isConflict());

        assertThat(account(fdaId).getLastAccrualDt()).isNull();
        assertThat(account(fdaId).getSts()).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM FD_BATCH_RUN_LOG", Integer.class)).isZero();
    }

    @Test
    void legacyAccountWithoutARateStillMaturesAndPaysBackThePrincipal() throws Exception {
        UUID fdaId = createAccount(accountJson("50000.00", 1, "RATE-1M", "COMPOUND", "MONTHLY", "ON_MATURITY"));
        removeContractedRate(fdaId);

        runBatch("MATURITY", "2026-02-01")
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.readCnt").value(1));

        FdAccount closed = account(fdaId);
        assertThat(closed.getSts()).isEqualTo("MATURED_CLOSED");
        assertThat(closed.getPrincipalBal()).isEqualByComparingTo("0.00");
        assertThat(transactions(fdaId)).extracting(FdTransaction::getTxnTyp)
                .containsExactly("DEPOSIT", "MATURITY_PAYOUT");
        assertThat(transactions(fdaId).get(1).getAmt()).isEqualByComparingTo("50000.00");
        assertLedgerReconciles();
    }

    @Test
    void scheduledEndOfDayRunsAccrualAndMaturityForTheBusinessDateThenAdvancesTheClock() throws Exception {
        UUID fdaId = createAccount(accountJson("100000.00", 1, "RATE-1M", "COMPOUND", "MONTHLY", "ON_MATURITY"));
        assertThat(account(fdaId).getMatAmt()).isEqualByComparingTo("100552.05");
        businessClock.setBusinessDate(date("2026-02-01"));

        // The 00:00 trigger on 2 Feb (IST): processes business date 1 Feb
        assertThat(scheduler().catchUpTo(date("2026-02-02"))).isEqualTo(1);

        FdBusinessClock clock = businessClock.getClock();
        assertThat(clock.getBusinessDt()).isEqualTo(date("2026-02-02"));
        assertThat(clock.getPrevBusinessDt()).isEqualTo(date("2026-02-01"));
        assertThat(clock.getEodSts()).isEqualTo("OPEN");

        FdAccount matured = account(fdaId);
        assertThat(matured.getSts()).isEqualTo("MATURED_CLOSED");
        assertThat(matured.getClosureDt()).isEqualTo(date("2026-02-01"));
        assertThat(transactions(fdaId)).extracting(FdTransaction::getTxnTyp)
                .containsExactly("DEPOSIT", "INTEREST", "MATURITY_PAYOUT");
        assertThat(transactions(fdaId).get(2).getAmt()).isEqualByComparingTo("100552.05");

        assertThat(jdbc.queryForList(
                "SELECT FDJB_JOB_NAME FROM FD_BATCH_RUN_LOG WHERE FDJB_BUSINESS_DT = '2026-02-01' ORDER BY FDJB_ID",
                String.class)).containsExactly("ACCRUAL", "MATURITY");
        assertLedgerReconciles();
    }

    @Test
    void depositOnTheThirteenthHasThatDaysInterestAccruedByTheMidnightRunOnTheFourteenth() throws Exception {
        businessClock.setBusinessDate(date("2026-09-13"));
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        FdAccount booked = account(fdaId);
        assertThat(booked.getValueDt()).isEqualTo(date("2026-09-13"));
        assertThat(booked.getDayCountConv()).isEqualTo("ACT/ACT");

        // 00:00 on 14 Sep: the calendar says 14th, the business date is still 13th
        int cycles = scheduler().catchUpTo(date("2026-09-14"));

        assertThat(cycles).isEqualTo(1);
        FdAccount accrued = account(fdaId);
        assertThat(accrued.getAccruedIntAmt()).isEqualByComparingTo("17.8082");
        assertThat(accrued.getLastAccrualDt()).isEqualTo(date("2026-09-13"));
        assertThat(businessClock.currentBusinessDate()).isEqualTo(date("2026-09-14"));
        // Accrued, not credited: no customer transaction, recognised in the GL
        assertThat(transactions(fdaId)).extracting(FdTransaction::getTxnTyp).containsExactly("DEPOSIT");
        assertThat(glBalances().get("FD_INT_EXP")).isEqualByComparingTo("17.81");
        assertThat(glBalances().get("FD_INT_PAYABLE")).isEqualByComparingTo("17.81");
        assertLedgerReconciles();

        // A second trigger the same night has nothing left to do
        assertThat(scheduler().catchUpTo(date("2026-09-14"))).isZero();
    }

    @Test
    void schedulerReplaysEveryMissedBusinessDateInOrder() throws Exception {
        businessClock.setBusinessDate(date("2026-09-10"));
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));

        // Service was down for three nights; the trigger at 00:00 on 14 Sep catches up 10–13 Sep
        assertThat(scheduler().catchUpTo(date("2026-09-14"))).isEqualTo(4);

        assertThat(businessClock.currentBusinessDate()).isEqualTo(date("2026-09-14"));
        assertThat(account(fdaId).getAccruedIntAmt()).isEqualByComparingTo("71.2329");
        assertThat(account(fdaId).getLastAccrualDt()).isEqualTo(date("2026-09-13"));
        assertThat(jdbc.queryForList("SELECT FDJB_BUSINESS_DT FROM FD_BATCH_RUN_LOG WHERE FDJB_JOB_NAME = 'ACCRUAL' "
                + "ORDER BY FDJB_ID", String.class))
                .containsExactly("2026-09-10", "2026-09-11", "2026-09-12", "2026-09-13");
        assertLedgerReconciles();
    }

    private EndOfDayScheduler scheduler() {
        return new EndOfDayScheduler(endOfDayService, businessClock, "Asia/Kolkata");
    }

    @Test
    void businessClockEndpointsReadSetAdvanceAndRunEndOfDay() throws Exception {
        mockMvc.perform(get("/business-clock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessDate").value("2026-01-01"));
        mockMvc.perform(put("/business-clock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessDate\":\"2026-06-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessDate").value("2026-06-30"))
                .andExpect(jsonPath("$.previousBusinessDate").value("2026-01-01"));
        mockMvc.perform(post("/business-clock/advance").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"days\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessDate").value("2026-07-02"));
        mockMvc.perform(post("/business-clock/advance").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"days\":0}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/business-clock/eod"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessDate").value("2026-07-02"))
                .andExpect(jsonPath("$.accrual.status").value("SUCCESS"))
                .andExpect(jsonPath("$.maturity.status").value("SUCCESS"))
                .andExpect(jsonPath("$.clock.businessDate").value("2026-07-03"));
    }

    @Test
    void invalidRequestsAreRejectedWithClearErrors() throws Exception {
        mockMvc.perform(post("/batch-jobs/{jobName}/runs", "INTEREST").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessDate\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/batch-jobs/{jobName}/runs", "ACCRUAL").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/fd-accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson("1000.00", 12, null, "COMPOUND", "QUARTERLY", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString("rateId")));
        assertThat(accountRepository.count()).isZero();

        mockMvc.perform(post("/fd-accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson("1000.00", 12, "RATE-12M", "COMPOUND", "WEEKLY", null)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_INTEREST_TERMS"));

        UUID fdaId = createAccount(accountJson("1000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", null));
        mockMvc.perform(post("/fd-accounts/{id}/interest-calculations", fdaId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodStart\":\"2026-05-01\",\"periodEnd\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void glAccountsEndpointListsTheSeededLedgerCodes() throws Exception {
        mockMvc.perform(get("/gl-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].glCd", hasItem("FD_DEP_LIAB")))
                .andExpect(jsonPath("$[*].glCd", hasItem("FD_INT_EXP")))
                .andExpect(jsonPath("$[*].glCd", hasItem("FD_SETTLEMENT")))
                .andExpect(jsonPath("$[*].glCd", hasItem("FD_PENALTY_INC")));
    }
}
