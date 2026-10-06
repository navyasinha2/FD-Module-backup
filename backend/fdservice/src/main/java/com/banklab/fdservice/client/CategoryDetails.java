package com.banklab.fdservice.client;

/**
 * Projection of Group 2's CUSTOMER_CATEGORY row identified by categoryCd.
 */
public record CategoryDetails(
        String categoryCode,
        String categoryName) {
}
