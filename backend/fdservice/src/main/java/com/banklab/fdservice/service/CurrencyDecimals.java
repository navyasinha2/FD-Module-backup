package com.banklab.fdservice.service;

import java.util.Currency;
import java.util.Locale;

/**
 * Minor-unit digits for an ISO 4217 code (INR 2, JPY 0, KWD 3) — the same value G2's
 * CURRENCY.decimal_places carries, taken from the JDK's ISO 4217 table while G2's
 * service is stubbed. Snapshotted onto FDA_CCY_DECIMALS at booking.
 */
public final class CurrencyDecimals {

    public static final int DEFAULT_DECIMALS = 2;

    private CurrencyDecimals() {
    }

    public static int forCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_DECIMALS;
        }
        try {
            int digits = Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT)).getDefaultFractionDigits();
            return digits < 0 ? DEFAULT_DECIMALS : digits;
        } catch (IllegalArgumentException e) {
            return DEFAULT_DECIMALS;
        }
    }
}
