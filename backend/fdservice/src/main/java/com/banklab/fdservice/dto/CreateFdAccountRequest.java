package com.banklab.fdservice.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Matches openapi schema CreateFdAccountRequest (POST /fd-accounts). productCode /
 * rateId / categoryCd are resolved against the Product & Pricing service at
 * creation time and snapshotted onto FD_ACCOUNTS — see client.ProductPricingClient.
 *
 * rateId is required: every deposit is booked with a contracted rate (lab manual
 * L12/L13). Once Group 2 can resolve the applicable rate from the product code, this
 * can be filled in server-side instead.
 *
 * interestType is optional: when omitted it comes from the product's
 * defaultInterestType, and if G2 doesn't supply one, from
 * fd.interest.default-interest-type (see ERD §14 open items).
 */
public record CreateFdAccountRequest(
        @NotBlank String custId,
        @NotBlank @Size(max = 20, message = "must be at most 20 characters") String productCode,
        @NotNull @Positive BigDecimal principal,
        @NotNull @Positive Integer tenureMonths,
        @NotBlank @Size(max = 20, message = "must be at most 20 characters") String rateId,
        String categoryCd,
        String currencyCode,
        String payoutFreq,
        String compoundingFreq,
        InterestType interestType,
        MaturityInstruction maturityInstruction,
        @NotEmpty List<@Valid AssignRoleRequest> initialRoles) {
}
