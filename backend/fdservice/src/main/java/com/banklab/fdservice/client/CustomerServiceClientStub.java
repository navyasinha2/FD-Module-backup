package com.banklab.fdservice.client;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.banklab.fdservice.client.UpstreamServiceException.Reason;

/**
 * Stand-in for Group 1's customer/auth service, which hasn't shipped yet (see
 * CustomerServiceClient). Returns hardcoded valid customers for the fixed IDs
 * the rest of the codebase's tests/fixtures already use, and falls back to
 * synthesizing a valid customer for any other non-blank ID so the frontend and
 * ad-hoc testing aren't stuck typing one of a handful of magic values. Rejects
 * only a blank ID, so downstream error-handling (404 mapping) still has a path
 * to exercise.
 */
@Component
public class CustomerServiceClientStub implements CustomerServiceClient {

    private static final Map<String, CustomerDto> KNOWN_CUSTOMERS = Map.of(
            "CUST-001", new CustomerDto("CUST-001", "Asha Rao", "ACTIVE"),
            "CUST-002", new CustomerDto("CUST-002", "Vikram Shah", "ACTIVE"),
            "CUST-003", new CustomerDto("CUST-003", "Priya Nair", "ACTIVE"));

    @Override
    public CustomerDto getCustomer(String customerId) {
        if (!StringUtils.hasText(customerId)) {
            throw new CustomerServiceClientException(
                    "Customer not found for customerId=" + customerId, Reason.NOT_FOUND);
        }
        CustomerDto known = KNOWN_CUSTOMERS.get(customerId);
        if (known != null) {
            return known;
        }
        return new CustomerDto(customerId, "Customer " + customerId, "ACTIVE");
    }
}
