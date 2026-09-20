package com.trace.risk.rule.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleType;

public record FraudRuleResponse(
        Long id,
        String ruleName,
        FraudRuleType ruleType,
        BigDecimal threshold,
        BigDecimal weight,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public static FraudRuleResponse from(FraudRule rule) {
        return new FraudRuleResponse(
                rule.getId(),
                rule.getRuleName(),
                rule.getRuleType(),
                rule.getThreshold(),
                rule.getWeight(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt());
    }
}