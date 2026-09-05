package com.trace.risk.evaluator;

import java.math.BigDecimal;

public record FraudRuleResult(
        boolean triggered,
        BigDecimal riskContribution,
        String reason
) {

    public static FraudRuleResult triggered(
            BigDecimal riskContribution,
            String reason
    ) {
        return new FraudRuleResult(
                true,
                riskContribution,
                reason
        );
    }

    public static FraudRuleResult notTriggered() {
        return new FraudRuleResult(
                false,
                BigDecimal.ZERO,
                null
        );
    }
}
