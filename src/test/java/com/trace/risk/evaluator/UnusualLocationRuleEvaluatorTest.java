package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleType;
import com.trace.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnusualLocationRuleEvaluatorTest {

    private final UnusualLocationRuleEvaluator evaluator =
            new UnusualLocationRuleEvaluator();

    @Test
    void shouldTriggerWhenCurrentLocationIsNotHistorical() {
        FraudRule rule = locationRule("20.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "Mumbai",
                        List.of("Pune", "Delhi", "Bengaluru")
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("20.00");
        assertThat(result.reason())
                .isEqualTo(
                        "Transaction location is not present in the customer's historical locations."
                );
    }

    @Test
    void shouldNotTriggerWhenCurrentLocationIsHistorical() {
        FraudRule rule = locationRule("20.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "Pune",
                        List.of("Pune", "Delhi", "Bengaluru")
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldCompareLocationsCaseInsensitively() {
        FraudRule rule = locationRule("20.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "mUmBaI",
                        List.of("Mumbai", "Delhi")
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldIgnoreWhitespaceAroundCurrentLocation() {
        FraudRule rule = locationRule("20.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "  Pune  ",
                        List.of("Pune", "Delhi")
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldNotTriggerWhenHistoricalLocationsAreEmpty() {
        FraudRule rule = locationRule("20.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "Mumbai",
                        Collections.emptyList()
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldNotTriggerWhenCurrentLocationIsMissing() {
        FraudRule rule = locationRule("20.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        List.of("Pune", "Delhi")
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldIgnoreNullAndBlankHistoricalLocations() {
        FraudRule rule = locationRule("20.00");

        List<String> historicalLocations =
                new java.util.ArrayList<>();

        historicalLocations.add(null);
        historicalLocations.add("");
        historicalLocations.add("   ");
        historicalLocations.add("Pune");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "Mumbai",
                        historicalLocations
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("20.00");
    }

    @Test
    void shouldUseConfiguredWeightInsteadOfHardcodedWeight() {
        FraudRule rule = locationRule("42.50");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "Mumbai",
                        List.of("Pune", "Delhi")
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("42.50");
    }

    private FraudRule locationRule(String weight) {
        return new FraudRule(
                "Unusual Location",
                FraudRuleType.UNUSUAL_LOCATION,
                BigDecimal.ZERO,
                new BigDecimal(weight),
                true
        );
    }

    private Transaction transaction() {
        return new Transaction(
                "TEST-TXN-LOCATION-001",
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