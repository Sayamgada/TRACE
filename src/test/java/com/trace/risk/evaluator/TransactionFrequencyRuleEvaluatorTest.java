package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleType;
import com.trace.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionFrequencyRuleEvaluatorTest {

    private final TransactionFrequencyRuleEvaluator evaluator =
            new TransactionFrequencyRuleEvaluator();

    @Test
    void shouldNotTriggerWhenTransactionCountEqualsThreshold() {
        Instant evaluationTime = Instant.parse("2026-09-05T10:00:00Z");

        FraudRule rule = frequencyRule("5", "25.00");

        List<Instant> recentTransactions = List.of(
                evaluationTime.minusSeconds(20),
                evaluationTime.minusSeconds(40),
                evaluationTime.minusSeconds(60),
                evaluationTime.minusSeconds(80),
                evaluationTime.minusSeconds(100)
        );

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        evaluationTime,
                        recentTransactions
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldTriggerWhenTransactionCountExceedsThreshold() {
        Instant evaluationTime = Instant.parse("2026-09-05T10:00:00Z");

        FraudRule rule = frequencyRule("5", "25.00");

        List<Instant> recentTransactions = List.of(
                evaluationTime.minusSeconds(20),
                evaluationTime.minusSeconds(40),
                evaluationTime.minusSeconds(60),
                evaluationTime.minusSeconds(80),
                evaluationTime.minusSeconds(100),
                evaluationTime.minusSeconds(110)
        );

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        evaluationTime,
                        recentTransactions
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("25.00");
        assertThat(result.reason())
                .isEqualTo(
                        "Transaction frequency exceeds the configured threshold within the evaluation window."
                );
    }

    @Test
    void shouldIgnoreTransactionsOutsideTwoMinuteWindow() {
        Instant evaluationTime = Instant.parse("2026-09-05T10:00:00Z");

        FraudRule rule = frequencyRule("5", "25.00");

        List<Instant> recentTransactions = List.of(
                evaluationTime.minusSeconds(20),
                evaluationTime.minusSeconds(40),
                evaluationTime.minusSeconds(60),
                evaluationTime.minusSeconds(80),
                evaluationTime.minusSeconds(100),
                evaluationTime.minusSeconds(121)
        );

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        evaluationTime,
                        recentTransactions
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
    }

    @Test
    void shouldUseConfiguredWeightInsteadOfHardcodedWeight() {
        Instant evaluationTime = Instant.parse("2026-09-05T10:00:00Z");

        FraudRule rule = frequencyRule("5", "42.50");

        List<Instant> recentTransactions = List.of(
                evaluationTime.minusSeconds(20),
                evaluationTime.minusSeconds(40),
                evaluationTime.minusSeconds(60),
                evaluationTime.minusSeconds(80),
                evaluationTime.minusSeconds(100),
                evaluationTime.minusSeconds(110)
        );

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        evaluationTime,
                        recentTransactions
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("42.50");
    }

    private FraudRule frequencyRule(
            String threshold,
            String weight
    ) {
        return new FraudRule(
                "Transaction Frequency",
                FraudRuleType.TRANSACTION_FREQUENCY,
                new BigDecimal(threshold),
                new BigDecimal(weight),
                true
        );
    }

    private Transaction transaction() {
        return new Transaction(
                "TEST-TXN-FREQUENCY-001",
                null,
                null,
                new BigDecimal("1000.00"),
                "INR",
                null,
                null,
                BigDecimal.ZERO
        );
    }
}