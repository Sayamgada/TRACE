package com.trace.risk.scoring;

import java.math.BigDecimal;
import java.util.List;

public record RiskEvaluationResult(
        BigDecimal riskScore,
        RiskLevel riskLevel,
        RiskDecision riskDecision,
        List<String> triggeredRules
) {

    public RiskEvaluationResult {
        triggeredRules = triggeredRules == null
                ? List.of()
                : List.copyOf(triggeredRules);
    }
}