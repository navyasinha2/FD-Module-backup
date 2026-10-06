package com.banklab.fdservice.dto;

/**
 * Matches openapi schema Error.
 */
public record ErrorResponse(String code, String message) {
}
