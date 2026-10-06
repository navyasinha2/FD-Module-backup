package com.banklab.fdservice.service;

/**
 * A posting targets a GL code with no active FD_GL_ACCOUNTS row. Maps to 500.
 */
public class GlAccountNotConfiguredException extends RuntimeException {

    public GlAccountNotConfiguredException(String message) {
        super(message);
    }
}
