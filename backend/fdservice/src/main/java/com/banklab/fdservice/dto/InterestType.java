package com.banklab.fdservice.dto;

/**
 * FDA_INT_TYP. Also decides the capitalization-vs-payout write path in the interest
 * batch: COMPOUND capitalizes at FDA_COMPOUND_FREQ boundaries (cumulative FD), SIMPLE
 * pays interest out at FDA_PAYOUT_FREQ boundaries (non-cumulative FD).
 */
public enum InterestType {
    SIMPLE,
    COMPOUND
}
