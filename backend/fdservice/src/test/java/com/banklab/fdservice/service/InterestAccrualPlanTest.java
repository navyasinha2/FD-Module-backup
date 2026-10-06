package com.banklab.fdservice.service;

import static com.banklab.fdservice.service.InterestCalculatorTest.compound;
import static com.banklab.fdservice.service.InterestCalculatorTest.date;
import static com.banklab.fdservice.service.InterestCalculatorTest.simple;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.banklab.fdservice.dto.InterestFrequency;

/**
 * Unit tests for per-account accrual planning — the logic the ACCRUAL / MATURITY batch
 * and premature closure apply. Pure: no Spring, no DB.
 */
class InterestAccrualPlanTest {

    private static final LocalDate VALUE_DATE = date("2026-01-01");
    private static final LocalDate MATURITY = date("2027-01-01");
    private static final BigDecimal PRINCIPAL = new BigDecimal("100000.00");

    private final InterestCalculator calculator = new InterestCalculator();
    private final InterestTerms quarterly = compound("6.5", InterestFrequency.QUARTERLY);

    @Test
    void midPeriodAccrualUpdatesAccruedInterestWithoutPostingAnything() {
        AccrualPlan plan = calculator.planAccrual(freshAccount(), quarterly, date("2026-02-15"));

        assertThat(plan.noOp()).isFalse();
        assertThat(plan.events()).isEmpty();
        assertThat(plan.accruedInterest()).isEqualByComparingTo("801.3699");
        assertThat(plan.accruedThrough()).isEqualTo(date("2026-02-15"));
        assertThat(plan.closingPrincipalBalance()).isEqualByComparingTo(PRINCIPAL);
        assertThat(plan.lastSettlementDate()).isNull();
    }

    @Test
    void rerunForAnAlreadyAccruedDateIsANoOp() {
        // Last accrual date 15 Feb = interest for 15 Feb is included (accrued through 16 Feb exclusive)
        AccountInterestState accrued = new AccountInterestState(PRINCIPAL, VALUE_DATE, MATURITY,
                date("2026-02-15"), null);

        assertThat(accrued.accruedThrough()).isEqualTo(date("2026-02-16"));
        assertThat(calculator.planAccrual(accrued, quarterly, date("2026-02-16")).noOp()).isTrue();
        assertThat(calculator.planAccrual(accrued, quarterly, date("2026-02-01")).noOp()).isTrue();
        assertThat(calculator.planAccrual(accrued, quarterly, date("2026-02-17")).noOp()).isFalse();
    }

    @Test
    void depositOnTheThirteenthHasThatDaysInterestAccruedByTheMidnightRun() {
        LocalDate depositDay = date("2026-09-13");
        AccountInterestState deposit = new AccountInterestState(PRINCIPAL, depositDay, date("2027-09-13"), null, null);
        InterestTerms actAct = InterestCalculatorTest.terms("6.5", com.banklab.fdservice.dto.InterestType.COMPOUND,
                InterestFrequency.QUARTERLY, DayCountConvention.ACT_ACT);

        // The EOD for business date 13 Sep (run at 00:00 on 14 Sep) accrues up to 14 Sep, exclusive
        LocalDate target = InterestCalculator.endOfDayAccrualTarget(depositDay, deposit.maturityDate());
        AccrualPlan plan = calculator.planAccrual(deposit, actAct, target);

        assertThat(target).isEqualTo(date("2026-09-14"));
        assertThat(plan.accruedInterest()).isEqualByComparingTo("17.8082");
        assertThat(plan.lastAccrualDate()).isEqualTo(depositDay);
        assertThat(plan.events()).isEmpty();
    }

    @Test
    void endOfDayTargetNeverPassesMaturity() {
        assertThat(InterestCalculator.endOfDayAccrualTarget(date("2026-12-30"), MATURITY)).isEqualTo(date("2026-12-31"));
        assertThat(InterestCalculator.endOfDayAccrualTarget(date("2026-12-31"), MATURITY)).isEqualTo(MATURITY);
        assertThat(InterestCalculator.endOfDayAccrualTarget(MATURITY, MATURITY)).isEqualTo(MATURITY);
    }

    @Test
    void businessDateOnOrBeforeTheValueDateIsANoOp() {
        assertThat(calculator.planAccrual(freshAccount(), quarterly, VALUE_DATE).noOp()).isTrue();
        assertThat(calculator.planAccrual(freshAccount(), quarterly, date("2025-12-31")).noOp()).isTrue();
    }

    @Test
    void crossingACompoundingBoundaryCapitalizesAndRestartsAccrualOnTheNewBalance() {
        AccountInterestState accrued = new AccountInterestState(PRINCIPAL, VALUE_DATE, MATURITY,
                date("2026-02-15"), null);

        AccrualPlan plan = calculator.planAccrual(accrued, quarterly, date("2026-05-01"));

        assertThat(plan.events()).singleElement().satisfies(event -> {
            assertThat(event.boundaryDate()).isEqualTo(date("2026-04-01"));
            assertThat(event.type()).isEqualTo(InterestEventType.CAPITALIZATION);
            assertThat(event.amount()).isEqualByComparingTo("1602.74");
        });
        assertThat(plan.closingPrincipalBalance()).isEqualByComparingTo("101602.74");
        assertThat(plan.lastSettlementDate()).isEqualTo(date("2026-04-01"));
        // 30 days (Apr 1 → May 1) on the capitalized balance
        assertThat(plan.accruedInterest()).isEqualByComparingTo(simpleInterest("101602.74", "6.5", 30));
    }

    @Test
    void missedRunsCatchUpAcrossSeveralBoundariesInOnePlan() {
        AccrualPlan plan = calculator.planAccrual(freshAccount(), quarterly, date("2026-10-15"));

        assertThat(plan.events()).extracting(InterestEvent::amount)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("1602.74"), new BigDecimal("1646.52"), new BigDecimal("1691.59"));
        assertThat(plan.closingPrincipalBalance()).isEqualByComparingTo("104940.85");
        assertThat(plan.accruedInterest()).isEqualByComparingTo(simpleInterest("104940.85", "6.5", 14));
    }

    @Test
    void accrualNeverRunsPastMaturity() {
        AccrualPlan plan = calculator.planAccrual(freshAccount(), quarterly, date("2027-03-01"));

        assertThat(plan.accruedThrough()).isEqualTo(MATURITY);
        assertThat(plan.events()).hasSize(4);
        assertThat(plan.closingPrincipalBalance()).isEqualByComparingTo("106660.15");
        assertThat(plan.accruedInterest()).isEqualByComparingTo("0");
    }

    @Test
    void simpleInterestWithPayoutFrequencyPlansPayoutsAndLeavesTheBalanceAlone() {
        AccountInterestState state = new AccountInterestState(new BigDecimal("120000.00"), VALUE_DATE,
                date("2026-04-01"), null, null);

        AccrualPlan plan = calculator.planAccrual(state, simple("6.5", InterestFrequency.MONTHLY), date("2026-03-10"));

        assertThat(plan.events()).extracting(InterestEvent::type)
                .containsOnly(InterestEventType.PAYOUT);
        assertThat(plan.events()).extracting(InterestEvent::amount)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("662.47"), new BigDecimal("598.36"));
        assertThat(plan.closingPrincipalBalance()).isEqualByComparingTo("120000.00");
        assertThat(plan.accruedInterest()).isEqualByComparingTo(simpleInterest("120000.00", "6.5", 9));
    }

    // --- The batch must reproduce the booking-time quote exactly --------------------

    @Test
    void dailyBatchAccrualReproducesTheQuotedMaturityForCompoundQuarterly() {
        assertDailyBatchMatchesQuote(PRINCIPAL, quarterly, VALUE_DATE, MATURITY);
    }

    @Test
    void dailyBatchAccrualReproducesTheQuotedMaturityForCompoundMonthlyFromTheThirtyFirst() {
        assertDailyBatchMatchesQuote(new BigDecimal("50000.00"), compound("7.25", InterestFrequency.MONTHLY),
                date("2026-01-31"), date("2026-07-31"));
    }

    @Test
    void dailyBatchAccrualReproducesTheQuotedInterestForSimpleWithMonthlyPayout() {
        assertDailyBatchMatchesQuote(new BigDecimal("120000.00"), simple("6.5", InterestFrequency.MONTHLY),
                VALUE_DATE, date("2026-04-01"));
    }

    @Test
    void dailyBatchAccrualReproducesTheQuotedInterestForSimpleHeldToMaturityOverALeapYear() {
        assertDailyBatchMatchesQuote(new BigDecimal("75000.00"), simple("7.1", null),
                date("2027-11-15"), date("2028-11-15"));
    }

    @Test
    void dailyBatchAccrualReproducesTheQuotedMaturityUnderActActAcrossALeapYear() {
        assertDailyBatchMatchesQuote(new BigDecimal("100000.00"),
                InterestCalculatorTest.terms("6.5", com.banklab.fdservice.dto.InterestType.COMPOUND,
                        InterestFrequency.QUARTERLY, DayCountConvention.ACT_ACT),
                date("2027-11-15"), date("2028-11-15"));
    }

    private void assertDailyBatchMatchesQuote(BigDecimal principal, InterestTerms terms, LocalDate valueDate,
            LocalDate maturityDate) {
        BigDecimal balance = principal;
        BigDecimal accrued = BigDecimal.ZERO;
        BigDecimal settled = BigDecimal.ZERO;
        LocalDate lastAccrual = null;
        LocalDate lastSettlement = null;

        // One EOD per business date from the deposit day to maturity, as the scheduler runs them
        for (LocalDate day = valueDate; !day.isAfter(maturityDate); day = day.plusDays(1)) {
            AccrualPlan plan = calculator.planAccrual(
                    new AccountInterestState(balance, valueDate, maturityDate, lastAccrual, lastSettlement), terms,
                    InterestCalculator.endOfDayAccrualTarget(day, maturityDate));
            if (plan.noOp()) {
                continue;
            }
            for (InterestEvent event : plan.events()) {
                settled = settled.add(event.amount());
            }
            balance = plan.closingPrincipalBalance();
            accrued = plan.accruedInterest();
            lastAccrual = plan.lastAccrualDate();
            lastSettlement = plan.lastSettlementDate();
        }

        InterestCalculation quote = calculator.calculate(principal, terms, valueDate, maturityDate);
        BigDecimal finalInterest = terms.roundToCurrency(accrued);
        assertThat(balance.add(finalInterest)).isEqualByComparingTo(quote.maturityValue());
        assertThat(settled.add(finalInterest)).isEqualByComparingTo(quote.totalInterest());
    }

    private static AccountInterestState freshAccount() {
        return new AccountInterestState(PRINCIPAL, VALUE_DATE, MATURITY, null, null);
    }

    /** base × rate × days / 36500 at accrual scale — the ACT/365 formula written out independently. */
    private static BigDecimal simpleInterest(String base, String rate, long days) {
        return new BigDecimal(base).multiply(new BigDecimal(rate)).multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(36500), InterestCalculator.ACCRUAL_SCALE, RoundingMode.HALF_UP);
    }
}
