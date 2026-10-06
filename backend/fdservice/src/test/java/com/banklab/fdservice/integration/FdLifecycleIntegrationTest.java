package com.banklab.fdservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdTransaction;
import com.banklab.fdservice.support.FdIntegrationTestSupport;

/**
 * End-to-end: Create FD → View FD → Interest preview → Accrual (and its idempotent
 * re-run) → Capitalization → Maturity → Closure, verified through the API and
 * directly against FD_ACCOUNTS / FD_TRANSACTIONS / FD_GL_ENTRIES / FD_GL_ACCOUNTS.
 */
class FdLifecycleIntegrationTest extends FdIntegrationTestSupport {

    @Test
    void compoundFixedDepositFromCreationToMaturityClosure() throws Exception {
        // 1. Create — ₹1,00,000 for 12 months at 6.50%, compounded quarterly
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));

        FdAccount created = account(fdaId);
        assertThat(created.getSts()).isEqualTo("ACTIVE");
        assertThat(created.getOpenDt()).isEqualTo(START);
        assertThat(created.getMatDt()).isEqualTo(date("2027-01-01"));
        assertThat(created.getIntTyp()).isEqualTo("COMPOUND");
        assertThat(created.getIntRt()).isEqualByComparingTo("6.50");
        assertThat(created.getMatAmt()).isEqualByComparingTo("106660.15");
        assertThat(transactions(fdaId)).singleElement().satisfies(deposit -> {
            assertThat(deposit.getTxnTyp()).isEqualTo("DEPOSIT");
            assertThat(deposit.getBalAfter()).isEqualByComparingTo("100000.00");
        });
        assertThat(glBalances().get("FD_DEP_LIAB")).isEqualByComparingTo("100000.00");

        // 2. View
        mockMvc.perform(get("/fd-accounts/{id}", fdaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctNum").value("FD001000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.interestType").value("COMPOUND"))
                .andExpect(jsonPath("$.matDt").value("2027-01-01"))
                .andExpect(jsonPath("$.matAmt").value(106660.15));
        mockMvc.perform(get("/fd-accounts").param("acctNum", "FD001000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fdaId").value(fdaId.toString()));
        mockMvc.perform(get("/fd-accounts/{id}/transactions", fdaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].txnType").value("DEPOSIT"));

        // 3. Interest preview — computes, persists nothing
        mockMvc.perform(post("/fd-accounts/{id}/interest-calculations", fdaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodStart\":\"2026-01-01\",\"periodEnd\":\"2027-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestAmount").value(6660.15))
                .andExpect(jsonPath("$.maturityValue").value(106660.15))
                .andExpect(jsonPath("$.dayCountConvention").value("ACT/ACT"))
                .andExpect(jsonPath("$.currencyCode").value("INR"))
                .andExpect(jsonPath("$.days").value(365));
        assertThat(transactions(fdaId)).hasSize(1);

        // 4. EOD for 15 Feb accrues every day up to and including 15 Feb (46 days); no customer transaction
        //    (the GL recognises it as interest payable)
        runBatch("ACCRUAL", "2026-02-15")
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.readCnt").value(1))
                .andExpect(jsonPath("$.writeCnt").value(1));
        FdAccount accrued = account(fdaId);
        assertThat(accrued.getAccruedIntAmt()).isEqualByComparingTo("819.1781");
        assertThat(accrued.getLastAccrualDt()).isEqualTo(date("2026-02-15"));
        assertThat(accrued.getPrincipalBal()).isEqualByComparingTo("100000.00");
        assertThat(transactions(fdaId)).hasSize(1);

        // 5. Duplicate trigger for the same business date is a no-op, not an error
        runBatch("ACCRUAL", "2026-02-15")
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.readCnt").value(0));
        assertThat(account(fdaId).getAccruedIntAmt()).isEqualByComparingTo("819.1781");
        assertThat(transactions(fdaId)).hasSize(1);

        // 6. First quarter boundary: interest capitalized into principal
        runBatch("ACCRUAL", "2026-04-01").andExpect(jsonPath("$.readCnt").value(1));
        FdAccount capitalized = account(fdaId);
        assertThat(capitalized.getPrincipalBal()).isEqualByComparingTo("101602.74");
        // ...and 1 Apr itself has already earned a day on the new balance
        assertThat(capitalized.getAccruedIntAmt()).isEqualByComparingTo("18.0936");
        assertThat(glBalances().get("FD_INT_PAYABLE")).isEqualByComparingTo("18.09");
        assertThat(capitalized.getLastCaptlzDt()).isEqualTo(date("2026-04-01"));
        FdTransaction interest = transactions(fdaId).get(1);
        assertThat(interest.getTxnTyp()).isEqualTo("INTEREST");
        assertThat(interest.getDrCr()).isEqualTo("C");
        assertThat(interest.getAmt()).isEqualByComparingTo("1602.74");
        assertThat(interest.getBalBefore()).isEqualByComparingTo("100000.00");
        assertThat(interest.getBalAfter()).isEqualByComparingTo("101602.74");
        assertThat(interest.getValueDt()).isEqualTo(date("2026-04-01"));

        // 7. Maturity batch before the maturity date processes nothing
        runBatch("MATURITY", "2026-12-31").andExpect(jsonPath("$.readCnt").value(0));
        assertThat(account(fdaId).getSts()).isEqualTo("ACTIVE");

        // 8. Maturity: remaining quarters capitalized, full balance paid out, account closed
        businessClock.setBusinessDate(date("2027-01-01"));
        runBatch("MATURITY", "2027-01-01")
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.readCnt").value(1))
                .andExpect(jsonPath("$.writeCnt").value(1));

        FdAccount closed = account(fdaId);
        assertThat(closed.getSts()).isEqualTo("MATURED_CLOSED");
        assertThat(closed.getClosureDt()).isEqualTo(date("2027-01-01"));
        assertThat(closed.getPrincipalBal()).isEqualByComparingTo("0.00");

        List<FdTransaction> ledger = transactions(fdaId);
        assertThat(ledger).extracting(FdTransaction::getTxnTyp)
                .containsExactly("DEPOSIT", "INTEREST", "INTEREST", "INTEREST", "INTEREST", "MATURITY_PAYOUT");
        assertThat(ledger).extracting(FdTransaction::getAmt)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("100000.00"), new BigDecimal("1602.74"), new BigDecimal("1646.52"),
                        new BigDecimal("1691.59"), new BigDecimal("1719.30"), new BigDecimal("106660.15"));
        // The batch paid exactly what was quoted at booking
        assertThat(ledger.get(5).getAmt()).isEqualByComparingTo(created.getMatAmt());
        assertThat(ledger.get(5).getBalAfter()).isEqualByComparingTo("0.00");

        mockMvc.perform(get("/fd-accounts/{id}", fdaId))
                .andExpect(jsonPath("$.status").value("MATURED_CLOSED"))
                .andExpect(jsonPath("$.closureDt").value("2027-01-01"));

        // 9. Re-running maturity doesn't pay twice
        runBatch("MATURITY", "2027-01-01").andExpect(jsonPath("$.readCnt").value(0));
        assertThat(transactions(fdaId)).hasSize(6);

        // 10. Ledger reconciles against FD_GL_ENTRIES and FD_GL_ACCOUNTS
        assertLedgerReconciles();
        assertThat(glBalances().get("FD_DEP_LIAB")).isEqualByComparingTo("0.00");
        assertThat(glBalances().get("FD_INT_EXP")).isEqualByComparingTo("6660.15");
        assertThat(glBalances().get("FD_SETTLEMENT")).isEqualByComparingTo("-6660.15");
        assertThat(glBalances().get("FD_INT_PAYABLE")).isEqualByComparingTo("0.00");

        // 11. Every run is in FD_BATCH_RUN_LOG
        mockMvc.perform(get("/batch-jobs/ACCRUAL/runs")).andExpect(jsonPath("$", hasSize(3)));
        mockMvc.perform(get("/batch-jobs/MATURITY/runs"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].businessDt").value("2027-01-01"));
    }
}
