package com.banklab.fdservice.client;

import lombok.Getter;

/**
 * Common shape for failures resolving data from a sibling microservice over REST
 * (fd_db holds no FK across service boundaries — see ERD principle P2).
 * {@link #getReason()} distinguishes "the entity genuinely doesn't exist" from
 * "the service itself is unreachable/erroring", so callers and
 * GlobalExceptionHandler don't map a genuine outage to the same HTTP status as an
 * actual missing entity. {@link ProductPricingClientException} and
 * {@link CustomerServiceClientException} both extend this rather than duplicating
 * the reason/message/cause plumbing.
 */
@Getter
public abstract class UpstreamServiceException extends RuntimeException {

    public enum Reason {
        NOT_FOUND,
        UPSTREAM_ERROR
    }

    private final Reason reason;

    protected UpstreamServiceException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    protected UpstreamServiceException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }
}
