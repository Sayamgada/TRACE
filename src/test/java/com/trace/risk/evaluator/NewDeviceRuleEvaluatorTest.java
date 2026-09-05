package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleType;
import com.trace.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NewDeviceRuleEvaluatorTest {

    private final NewDeviceRuleEvaluator evaluator =
            new NewDeviceRuleEvaluator();

    @Test
    void shouldTriggerWhenDeviceIsNotHistorical() {
        FraudRule rule = deviceRule("15.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                contextWithDevice(
                        "device-new-001",
                        "device-old-001",
                        "device-old-002"
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("15.00");
        assertThat(result.reason())
                .isEqualTo(
                        "Transaction is being performed from a device not previously associated with the customer."
                );
    }

    @Test
    void shouldNotTriggerWhenDeviceIsHistorical() {
        FraudRule rule = deviceRule("15.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                contextWithDevice(
                        "device-old-001",
                        "device-old-001",
                        "device-old-002"
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldNotTriggerWhenHistoricalDevicesAreEmpty() {
        FraudRule rule = deviceRule("15.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        Collections.emptyList(),
                        "device-new-001",
                        Collections.emptyList()
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldNotTriggerWhenCurrentDeviceIsMissing() {
        FraudRule rule = deviceRule("15.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        Collections.emptyList(),
                        null,
                        List.of("device-old-001")
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldIgnoreWhitespaceAroundCurrentDeviceId() {
        FraudRule rule = deviceRule("15.00");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                contextWithDevice(
                        "  device-old-001  ",
                        "device-old-001",
                        "device-old-002"
                )
        );

        assertThat(result.triggered()).isFalse();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("0");
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldIgnoreNullAndBlankHistoricalDeviceIds() {
        FraudRule rule = deviceRule("15.00");

        List<String> historicalDeviceIds =
                new ArrayList<>();

        historicalDeviceIds.add(null);
        historicalDeviceIds.add("");
        historicalDeviceIds.add("   ");
        historicalDeviceIds.add("device-old-001");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                new FraudEvaluationContext(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        Collections.emptyList(),
                        "device-new-001",
                        historicalDeviceIds
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("15.00");
    }

    @Test
    void shouldUseConfiguredWeightInsteadOfHardcodedWeight() {
        FraudRule rule = deviceRule("42.50");

        FraudRuleResult result = evaluator.evaluate(
                transaction(),
                rule,
                contextWithDevice(
                        "device-new-001",
                        "device-old-001"
                )
        );

        assertThat(result.triggered()).isTrue();
        assertThat(result.riskContribution())
                .isEqualByComparingTo("42.50");
    }

    private FraudEvaluationContext contextWithDevice(
            String currentDeviceId,
            String... historicalDeviceIds
    ) {
        return new FraudEvaluationContext(
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                currentDeviceId,
                List.of(historicalDeviceIds)
        );
    }

    private FraudRule deviceRule(String weight) {
        return new FraudRule(
                "New Device",
                FraudRuleType.NEW_DEVICE,
                BigDecimal.ZERO,
                new BigDecimal(weight),
                true
        );
    }

    private Transaction transaction() {
        return new Transaction(
                "TEST-TXN-DEVICE-001",
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