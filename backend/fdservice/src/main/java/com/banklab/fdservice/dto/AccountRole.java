package com.banklab.fdservice.dto;

import java.time.LocalDate;

/**
 * Matches openapi schema AccountRole — allOf [AssignRoleRequest, {fdrlId, fdaId,
 * efctvDt}], flattened into one response DTO.
 *
 * Not yet wired to a controller — this is the response shape for
 * "POST /fd-accounts/{accountId}/roles" (openapi: Action 6), which fd-service
 * hasn't implemented yet.
 */
public record AccountRole(
        String custId,
        RoleType roleType,
        Boolean isPrimary,
        String fdrlId,
        String fdaId,
        LocalDate efctvDt) {
}
