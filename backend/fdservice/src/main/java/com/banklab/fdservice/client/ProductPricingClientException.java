package com.banklab.fdservice.client;

/**
 * Raised when the Product & Pricing service (Group 2) cannot resolve a
 * productCode/rateId/categoryCd, or is unreachable.
 */
public class ProductPricingClientException extends UpstreamServiceException {

    public ProductPricingClientException(String message, Reason reason) {
        super(message, reason);
    }

    public ProductPricingClientException(String message, Reason reason, Throwable cause) {
        super(message, reason, cause);
    }
}
