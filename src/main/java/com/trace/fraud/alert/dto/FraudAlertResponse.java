package com.trace.fraud.alert.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;

public record FraudAlertResponse(
        Long id,
        Long transactionId,
        BigDecimal riskScore,
        FraudAlertSeverity severity,
        FraudAlertStatus status,
        String reasons,
        Instant createdAt
) {
}
