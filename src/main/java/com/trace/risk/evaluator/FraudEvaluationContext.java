package com.trace.risk.evaluator;

import java.time.Instant;

public record FraudEvaluationContext(
        Instant evaluationTime
) {
}