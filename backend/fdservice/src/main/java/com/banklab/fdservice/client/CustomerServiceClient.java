package com.banklab.fdservice.client;

/**
 * Client for the Customer/Auth service (Group 1). fd_db has no DB-level FK for
 * CUST_ID (separate database — ERD principle P2), so account creation resolves
 * the customer over REST and snapshots the result onto FD_ACCOUNTS instead of
 * joining live later.
 *
 * Group 1's service hasn't shipped yet, so {@link CustomerServiceClientStub} is
 * the only implementation for now. Once G1 is available, add a RestClient-based
 * CustomerServiceClientImpl (mirroring ProductPricingClientImpl) and swap the
 * stub's @Component for it — callers of this interface don't change.
 */
public interface CustomerServiceClient {

    /**
     * Resolves a customer by ID. Required for account creation — throws if not found.
     */
    CustomerDto getCustomer(String customerId);
}
