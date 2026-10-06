package com.banklab.fdservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Matches openapi schema AssignRoleRequest — used both standalone (POST
 * /fd-accounts/{accountId}/roles) and nested as CreateFdAccountRequest.initialRoles.
 */
public record AssignRoleRequest(
        @NotBlank String custId,
        @NotNull RoleType roleType,
        Boolean isPrimary) {
}
