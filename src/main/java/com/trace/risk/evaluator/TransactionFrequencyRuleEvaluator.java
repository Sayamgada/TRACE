package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class TransactionFrequencyRuleEvaluator implements FraudRuleEvaluator {

    private static final Duration EVALUATION_WINDOW = Duration.ofMinutes(2);

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRule rule,
            FraudEvaluationContext context
    ) {
        Instant evaluationTime = context.evaluationTime() != null
                ? context.evaluationTime()
                : transaction.getCreatedAt();

        if (evaluationTime == null) {
            return FraudRuleResult.notTriggered();
        }

        Instant windowStart = evaluationTime.minus(EVALUATION_WINDOW);

        List<Instant> recentTransactions =
                context.recentTransactionTimes()
                        .stream()
                        .filter(timestamp -> timestamp != null)
                        .filter(timestamp ->
                                !timestamp.isBefore(windowStart)
                                        && !timestamp.isAfter(evaluationTime)
                        )
                        .toList();

        BigDecimal transactionCount =
                BigDecimal.valueOf(recentTransactions.size());

        if (transactionCount.compareTo(rule.getThreshold()) > 0) {
            return FraudRuleResult.triggered(
                    rule.getWeight(),
                    "Transaction frequency exceeds the configured threshold within the evaluation window."
            );
        }

        return FraudRuleResult.notTriggered();
    }
}