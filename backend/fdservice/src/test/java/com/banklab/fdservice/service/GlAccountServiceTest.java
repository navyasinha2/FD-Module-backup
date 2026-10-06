package com.banklab.fdservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * Unit test for the GL balance sign convention used on FDGL_CURRENT_BAL.
 */
class GlAccountServiceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("100.00");

    @Test
    void debitsRaiseAssetAndExpenseBalancesAndCreditsLowerThem() {
        assertThat(GlAccountService.signedEffect("ASSET", true, AMOUNT)).isEqualByComparingTo("100.00");
        assertThat(GlAccountService.signedEffect("ASSET", false, AMOUNT)).isEqualByComparingTo("-100.00");
        assertThat(GlAccountService.signedEffect("EXPENSE", true, AMOUNT)).isEqualByComparingTo("100.00");
        assertThat(GlAccountService.signedEffect("EXPENSE", false, AMOUNT)).isEqualByComparingTo("-100.00");
    }

    @Test
    void creditsRaiseLiabilityAndIncomeBalancesAndDebitsLowerThem() {
        assertThat(GlAccountService.signedEffect("LIABILITY", false, AMOUNT)).isEqualByComparingTo("100.00");
        assertThat(GlAccountService.signedEffect("LIABILITY", true, AMOUNT)).isEqualByComparingTo("-100.00");
        assertThat(GlAccountService.signedEffect("INCOME", false, AMOUNT)).isEqualByComparingTo("100.00");
        assertThat(GlAccountService.signedEffect("INCOME", true, AMOUNT)).isEqualByComparingTo("-100.00");
    }
}
