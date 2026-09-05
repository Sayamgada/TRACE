package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleType;
import com.trace.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HighAmountRuleEvaluatorTest {

    private final HighAmountRuleEvaluator evaluator =
            new HighAmountRuleEvaluator();

    @Test
    void shouldTriggerWhenAmountExceedsThreshold() {
        FraudRule rule = new FraudRule(
                "High Amount",
                FraudRuleType.HIGH_AMOUNT,
                new BigDecimal("50000.00"),
                new BigDecimal("30.00"),
                true
        );

        Transaction transaction = transactionWithAmount("50000.01");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                new FraudEvaluationContext(null)
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("30.00");
        assertThat(result.reason())
                .isEqualTo(
                        "Transaction amount exceeds the configured high-amount threshold."
                );
    }

    @Test
    void shouldNotTriggerWhenAmountEqualsThreshold() {
        FraudRule rule = new FraudRule(
                "High Amount",
                FraudRuleType.HIGH_AMOUNT,
                new BigDecimal("50000.00"),
                new BigDecimal("30.00"),
                true
        );

        Transaction transaction = transactionWithAmount("50000.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                new FraudEvaluationContext(null)
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldNotTriggerWhenAmountIsBelowThreshold() {
        FraudRule rule = new FraudRule(
                "High Amount",
                FraudRuleType.HIGH_AMOUNT,
                new BigDecimal("50000.00"),
                new BigDecimal("30.00"),
                true
        );

        Transaction transaction = transactionWithAmount("49999.99");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                new FraudEvaluationContext(null)
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldUseConfiguredWeightInsteadOfHardcodedWeight() {
        FraudRule rule = new FraudRule(
                "High Amount",
                FraudRuleType.HIGH_AMOUNT,
                new BigDecimal("10000.00"),
                new BigDecimal("42.50"),
                true
        );

        Transaction transaction = transactionWithAmount("10000.01");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                new FraudEvaluationContext(null)
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("42.50");
    }

    private Transaction transactionWithAmount(String amount) {
        return new Transaction(
                "TEST-TXN-001",
                null,
                null,
                new BigDecimal(amount),
                "INR",
                null,
                null,
                BigDecimal.ZERO
        );
    }
}