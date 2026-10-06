package com.banklab.fdservice.service;

import com.banklab.fdservice.dto.RoleType;

/**
 * Raised when a set of roles being assigned to an account would exceed the
 * cardinality cap for a role type (GUARANTOR/GUARDIAN: one each). Maps to HTTP 409
 * per the createFdAccount / assignAccountRole contracts.
 */
public class RoleCardinalityViolationException extends RuntimeException {

    public RoleCardinalityViolationException(RoleType roleType) {
        super("At most one " + roleType + " role is allowed per account");
    }
}
