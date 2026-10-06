package com.banklab.fdservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Matches openapi schema FdTransaction (GET /fd-accounts/{accountId}/transactions).
 */
public record FdTransaction(
        Long fdtId,
        String fdaId,
        String txnGrpId,
        String txnType,
        String drCr,
        BigDecimal amt,
        BigDecimal balBefore,
        BigDecimal balAfter,
        String ccyCd,
        LocalDate txnDt,
        LocalDate valueDt,
        LocalDateTime txnTs,
        BigDecimal tdsRt,
        String remarks) {
}
