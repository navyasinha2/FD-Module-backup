package com.banklab.fdservice.service;

/**
 * Raised when a request supplies a categoryCd that the Product & Pricing service
 * can't resolve to a known customer category (ERD justification doc §14 — no
 * confirmed customer->category mapping exists yet from the Customer module).
 * Maps to HTTP 422 per the createFdAccount contract.
 */
public class CategoryMappingUnresolvedException extends RuntimeException {

    public CategoryMappingUnresolvedException(String categoryCd) {
        super("No customer category mapping resolved for categoryCd='" + categoryCd + "'");
    }
}
