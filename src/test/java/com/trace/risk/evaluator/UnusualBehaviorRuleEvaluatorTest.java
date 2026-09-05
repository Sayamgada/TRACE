package com.trace.risk.evaluator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleType;
import com.trace.transaction.entity.Transaction;

class UnusualBehaviorRuleEvaluatorTest {

    private final UnusualBehaviorRuleEvaluator evaluator
            = new UnusualBehaviorRuleEvaluator();

    @Test
    void shouldTriggerWhenAmountSignificantlyExceedsHistoricalAverage() {
        FraudRule rule = behaviorRule("3", "20.00");

        Transaction transaction = transactionWithAmount("35000.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                contextWithAmounts(
                        "10000.00",
                        "10000.00",
                        "10000.00"
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("20.00");
        assertThat(result.reason())
                .isEqualTo(
                        "Transaction amount is significantly above the customer's historical average behavior."
                );
    }

    @Test
    void shouldNotTriggerWhenAmountIsWithinConfiguredMultiplier() {
        FraudRule rule = behaviorRule("3", "20.00");

        Transaction transaction = transactionWithAmount("30000.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                contextWithAmounts(
                        "10000.00",
                        "10000.00",
                        "10000.00"
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldNotTriggerWhenHistoricalDataIsEmpty() {
        FraudRule rule = behaviorRule("3", "20.00");

        Transaction transaction = transactionWithAmount("50000.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldIgnoreNullAndNegativeHistoricalAmounts() {
        FraudRule rule = behaviorRule("3", "20.00");

        Transaction transaction = transactionWithAmount("35000.00");

        List<BigDecimal> historicalAmounts
                = new java.util.ArrayList<>();

        historicalAmounts.add(new BigDecimal("10000.00"));
        historicalAmounts.add(null);
        historicalAmounts.add(new BigDecimal("-5000.00"));
        historicalAmounts.add(new BigDecimal("10000.00"));
        historicalAmounts.add(new BigDecimal("10000.00"));

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        historicalAmounts
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("20.00");
    }

    @Test
    void shouldUseConfiguredWeightInsteadOfHardcodedWeight() {
        FraudRule rule = behaviorRule("3", "42.50");

        Transaction transaction = transactionWithAmount("35000.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                contextWithAmounts(
                        "10000.00",
                        "10000.00",
                        "10000.00"
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("42.50");
    }

    @Test
    void shouldNotTriggerWhenCurrentAmountIsBelowHistoricalAverageMultiplier() {
        FraudRule rule = behaviorRule("3", "20.00");

        Transaction transaction = transactionWithAmount("25000.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction,
                rule,
                contextWithAmounts(
                        "10000.00",
                        "10000.00",
                        "10000.00"
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
    }

    private FraudEvaluationContext contextWithAmounts(
            String... amounts
    ) {
        return new FraudEvaluationContext(
                null,
                Collections.emptyList(),
                List.of(
                        java.util.Arrays.stream(amounts)
                                .map(BigDecimal::new)
                                .toArray(BigDecimal[]::new)
                )
        );
    }

    private FraudRule behaviorRule(
            String threshold,
            String weight
    ) {
        return new FraudRule(
                "Unusual Behavior",
                FraudRuleType.UNUSUAL_BEHAVIOR,
                new BigDecimal(threshold),
                new BigDecimal(weight),
                true
        );
    }

    private Transaction transactionWithAmount(String amount) {
        return new Transaction(
                "TEST-TXN-BEHAVIOR-001",
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
