package com.finflow.transaction.dto.response;

import com.finflow.transaction.enums.FraudDecisionType;
import com.finflow.transaction.enums.FraudRuleCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudCheckResponse(
        UUID transactionId,
        FraudRuleCode ruleCode,
        FraudDecisionType decision,
        BigDecimal score,
        String reason,
        Instant checkedAt
) {
}
