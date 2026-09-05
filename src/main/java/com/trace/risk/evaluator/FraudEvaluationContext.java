package com.trace.risk.evaluator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record FraudEvaluationContext(
        Instant evaluationTime,
        List<Instant> recentTransactionTimes,
        List<BigDecimal> historicalTransactionAmounts
) {

    public FraudEvaluationContext {
        recentTransactionTimes =
                recentTransactionTimes == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(recentTransactionTimes)
                        );

        historicalTransactionAmounts =
                historicalTransactionAmounts == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(historicalTransactionAmounts)
                        );
    }

    public FraudEvaluationContext(Instant evaluationTime) {
        this(
                evaluationTime,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public FraudEvaluationContext(
            Instant evaluationTime,
            List<Instant> recentTransactionTimes
    ) {
        this(
                evaluationTime,
                recentTransactionTimes,
                Collections.emptyList()
        );
    }
}