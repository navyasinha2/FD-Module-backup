package com.banklab.fdservice.client;

import java.util.Optional;

/**
 * Stub client for the Product & Pricing service (Group 2). fd_db has no DB-level
 * FK for productCode / rateId / categoryCd (separate database — ERD principle P2),
 * so account creation resolves these over REST and snapshots the result onto
 * FD_ACCOUNTS instead of joining live later.
 */
public interface ProductPricingClient {

    /**
     * Resolves a product by code. Required for account creation — throws if not found.
     */
    ProductDetails getProduct(String productCode);

    /**
     * Resolves a specific PRODUCT_RATE_MATRIX row by rateId. Empty if rateId is
     * absent from the request or not found by Group 2.
     */
    Optional<RateDetails> getRate(String rateId);

    /**
     * Resolves a customer category. Empty if categoryCd is absent from the request
     * or not found by Group 2 (open item — see justification doc §14).
     */
    Optional<CategoryDetails> getCategory(String categoryCode);
}
