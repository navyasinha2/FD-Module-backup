package com.banklab.fdservice.dto;

/**
 * FDGL_TYP. Decides which side of a posting increases FDGL_CURRENT_BAL: debits
 * increase ASSET / EXPENSE balances, credits increase LIABILITY / INCOME balances.
 */
public enum GlAccountType {
    ASSET,
    LIABILITY,
    INCOME,
    EXPENSE;

    public boolean isDebitNormal() {
        return this == ASSET || this == EXPENSE;
    }
}
