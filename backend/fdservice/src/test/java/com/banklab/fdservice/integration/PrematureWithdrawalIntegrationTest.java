package com.banklab.fdservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdTransaction;
import com.banklab.fdservice.support.FdIntegrationTestSupport;

/**
 * End-to-end premature withdrawal (lab manual L15, L20 scenarios 2 and 3).
 */
class PrematureWithdrawalIntegrationTest extends FdIntegrationTestSupport {

    @Test
    void withdrawalCreditsInterestToDateAppliesPenaltyPaysOutAndClosesTheAccount() throws Exception {
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        businessClock.setBusinessDate(date("2026-07-16"));

        // Two quarters capitalized (1602.74 + 1646.52) + 15 days on 103249.26 (275.80) = 3525.06 earned;
        // 1% penalty = 35.25; payout = 103525.06 - 35.25 = 103489.81
        withdraw(fdaId, "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.withdrawalDate").value("2026-07-16"))
                .andExpect(jsonPath("$.interestEarned").value(3525.06))
                .andExpect(jsonPath("$.finalInterest").value(275.80))
                .andExpect(jsonPath("$.penaltyApplied").value(35.25))
                .andExpect(jsonPath("$.withdrawalAmount").value(103489.81))
                .andExpect(jsonPath("$.account.status").value("CLOSED"));

        FdAccount closed = account(fdaId);
        assertThat(closed.getSts()).isEqualTo("CLOSED");
        assertThat(closed.getClosureDt()).isEqualTo(date("2026-07-16"));
        assertThat(closed.getPrincipalBal()).isEqualByComparingTo("0.00");

        List<FdTransaction> ledger = transactions(fdaId);
        assertThat(ledger).extracting(FdTransaction::getTxnTyp)
                .containsExactly("DEPOSIT", "INTEREST", "INTEREST", "INTEREST", "PENALTY", "WITHDRAWAL");
        assertThat(ledger).extracting(FdTransaction::getAmt)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("100000.00"), new BigDecimal("1602.74"), new BigDecimal("1646.52"),
                        new BigDecimal("275.80"), new BigDecimal("35.25"), new BigDecimal("103489.81"));
        assertThat(ledger.get(4).getDrCr()).isEqualTo("D");
        assertThat(ledger.get(5).getDrCr()).isEqualTo("D");
        // Final interest, penalty and withdrawal are one business event
        assertThat(ledger.subList(3, 6)).extracting(FdTransaction::getTxnGrpId).containsOnly(ledger.get(5).getTxnGrpId());

        assertLedgerReconciles();
        assertThat(glBalances().get("FD_DEP_LIAB")).isEqualByComparingTo("0.00");
        assertThat(glBalances().get("FD_PENALTY_INC")).isEqualByComparingTo("35.25");
        assertThat(glBalances().get("FD_INT_EXP")).isEqualByComparingTo("3525.06");

        // Scenario 3: withdrawing a closed account fails and changes nothing
        withdraw(fdaId, "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVE"));
        assertThat(transactions(fdaId)).hasSize(6);

        // Batches never touch a prematurely closed account
        runBatch("ACCRUAL", "2026-08-01").andExpect(jsonPath("$.readCnt").value(0));
        runBatch("MATURITY", "2027-01-01").andExpect(jsonPath("$.readCnt").value(0));
        assertThat(account(fdaId).getSts()).isEqualTo("CLOSED");
        assertThat(transactions(fdaId)).hasSize(6);
    }

    @Test
    void withdrawalOnTheMaturityDateIsRejected() throws Exception {
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        businessClock.setBusinessDate(date("2027-01-01"));

        withdraw(fdaId, "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_MATURED"));
        assertThat(account(fdaId).getSts()).isEqualTo("ACTIVE");
    }

    @Test
    void partialWithdrawalIsRejectedAndNothingIsPosted() throws Exception {
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        businessClock.setBusinessDate(date("2026-03-01"));

        withdraw(fdaId, "{\"amount\": 5000}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PARTIAL_WITHDRAWAL_NOT_SUPPORTED"));
        assertThat(transactions(fdaId)).hasSize(1);
        assertThat(account(fdaId).getLastAccrualDt()).isNull();
    }

    @Test
    void withdrawalOnADateBeforeAlreadyProcessedAccrualIsRejected() throws Exception {
        UUID fdaId = createAccount(accountJson("100000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        runBatch("ACCRUAL", "2026-04-01").andExpect(jsonPath("$.readCnt").value(1));
        businessClock.setBusinessDate(date("2026-03-01"));

        withdraw(fdaId, "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCRUAL_AHEAD_OF_BUSINESS_DATE"));
        assertThat(account(fdaId).getSts()).isEqualTo("ACTIVE");
        assertThat(transactions(fdaId)).hasSize(2);
    }

    @Test
    void legacyAccountWithoutARateCanStillBeWithdrawnWithoutInterest() throws Exception {
        UUID fdaId = createAccount(accountJson("50000.00", 12, "RATE-12M", "COMPOUND", "QUARTERLY", "ON_MATURITY"));
        removeContractedRate(fdaId);
        businessClock.setBusinessDate(date("2026-05-01"));

        withdraw(fdaId, "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestEarned").value(0.0))
                .andExpect(jsonPath("$.penaltyApplied").value(0.0))
                .andExpect(jsonPath("$.withdrawalAmount").value(50000.0));

        assertThat(account(fdaId).getSts()).isEqualTo("CLOSED");
        assertThat(transactions(fdaId)).extracting(FdTransaction::getTxnTyp).containsExactly("DEPOSIT", "WITHDRAWAL");
        assertLedgerReconciles();
    }

    @Test
    void unknownOrMalformedAccountIdsAreRejected() throws Exception {
        withdraw(UUID.randomUUID(), "{}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mockMvc.perform(get("/fd-accounts/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get("/fd-accounts/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
