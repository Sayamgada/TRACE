package com.trace.risk.evaluator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record FraudEvaluationContext(
        Instant evaluationTime,
        List<Instant> recentTransactionTimes,
        List<BigDecimal> historicalTransactionAmounts,
        String currentLocation,
        List<String> historicalLocations,
        String currentDeviceId,
        List<String> historicalDeviceIds
        ) {

    public FraudEvaluationContext {
        recentTransactionTimes
                = recentTransactionTimes == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(recentTransactionTimes)
                        );

        historicalTransactionAmounts
                = historicalTransactionAmounts == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(historicalTransactionAmounts)
                        );

        historicalLocations
                = historicalLocations == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(historicalLocations)
                        );

        historicalDeviceIds
                = historicalDeviceIds == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(historicalDeviceIds)
                        );
    }

    public FraudEvaluationContext(Instant evaluationTime) {
        this(
                evaluationTime,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                null,
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
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                null,
                Collections.emptyList()
        );
    }

    public FraudEvaluationContext(
            Instant evaluationTime,
            List<Instant> recentTransactionTimes,
            List<BigDecimal> historicalTransactionAmounts
    ) {
        this(
                evaluationTime,
                recentTransactionTimes,
                historicalTransactionAmounts,
                null,
                Collections.emptyList(),
                null,
                Collections.emptyList()
        );
    }

    public FraudEvaluationContext(
            Instant evaluationTime,
            List<Instant> recentTransactionTimes,
            List<BigDecimal> historicalTransactionAmounts,
            String currentLocation,
            List<String> historicalLocations
    ) {
        this(
                evaluationTime,
                recentTransactionTimes,
                historicalTransactionAmounts,
                currentLocation,
                historicalLocations,
                null,
                Collections.emptyList()
        );
    }
}
