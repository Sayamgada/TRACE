package com.trace.risk.scoring;

import com.trace.risk.evaluator.FraudEvaluationContext;
import com.trace.risk.evaluator.FraudRuleEvaluator;
import com.trace.risk.evaluator.FraudRuleEvaluatorRegistry;
import com.trace.risk.evaluator.FraudRuleResult;
import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleRepository;
import com.trace.risk.rule.FraudRuleType;
import com.trace.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RiskEvaluationServiceTest {

    @Test
    void shouldApproveWhenRiskScoreIsBelowThirty() {
        FraudRule rule = rule(
                "High Amount",
                FraudRuleType.HIGH_AMOUNT,
                "50000.00",
                "29.99"
        );

        FraudRuleRepository repository = mock(FraudRuleRepository.class);
        FraudRuleEvaluatorRegistry registry =
                mock(FraudRuleEvaluatorRegistry.class);
        FraudRuleEvaluator evaluator =
                mock(FraudRuleEvaluator.class);

        when(repository.findByActiveTrue())
                .thenReturn(List.of(rule));

        when(registry.getEvaluator(FraudRuleType.HIGH_AMOUNT))
                .thenReturn(evaluator);

        when(evaluator.evaluate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(rule),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(
                FraudRuleResult.triggered(
                        new BigDecimal("29.99"),
                        "test"
                )
        );

        RiskEvaluationService service =
                new RiskEvaluationService(repository, registry);

        RiskEvaluationResult result =
                service.evaluate(
                        transaction(),
                        new FraudEvaluationContext(null)
                );

        assertThat(result.riskScore())
                .isEqualByComparingTo("29.99");
        assertThat(result.riskLevel())
                .isEqualTo(RiskLevel.LOW);
        assertThat(result.riskDecision())
                .isEqualTo(RiskDecision.APPROVE);
        assertThat(result.triggeredRules())
                .containsExactly("High Amount");
    }

    @Test
    void shouldRequireReviewAtThirty() {
        assertScoreBoundary(
                "30.00",
                RiskLevel.MEDIUM,
                RiskDecision.REVIEW
        );
    }

    @Test
    void shouldRequireReviewBelowSeventy() {
        assertScoreBoundary(
                "69.99",
                RiskLevel.MEDIUM,
                RiskDecision.REVIEW
        );
    }

    @Test
    void shouldBlockAtSeventy() {
        assertScoreBoundary(
                "70.00",
                RiskLevel.HIGH,
                RiskDecision.BLOCK
        );
    }

    @Test
    void shouldCapRiskScoreAtOneHundred() {
        FraudRule firstRule = rule(
                "Rule One",
                FraudRuleType.HIGH_AMOUNT,
                "1.00",
                "70.00"
        );

        FraudRule secondRule = rule(
                "Rule Two",
                FraudRuleType.NEW_DEVICE,
                "0.00",
                "50.00"
        );

        FraudRuleRepository repository = mock(FraudRuleRepository.class);
        FraudRuleEvaluatorRegistry registry =
                mock(FraudRuleEvaluatorRegistry.class);

        FraudRuleEvaluator firstEvaluator =
                mock(FraudRuleEvaluator.class);
        FraudRuleEvaluator secondEvaluator =
                mock(FraudRuleEvaluator.class);

        when(repository.findByActiveTrue())
                .thenReturn(List.of(firstRule, secondRule));

        when(registry.getEvaluator(FraudRuleType.HIGH_AMOUNT))
                .thenReturn(firstEvaluator);

        when(registry.getEvaluator(FraudRuleType.NEW_DEVICE))
                .thenReturn(secondEvaluator);

        when(firstEvaluator.evaluate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(firstRule),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(
                FraudRuleResult.triggered(
                        new BigDecimal("70.00"),
                        "test"
                )
        );

        when(secondEvaluator.evaluate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(secondRule),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(
                FraudRuleResult.triggered(
                        new BigDecimal("50.00"),
                        "test"
                )
        );

        RiskEvaluationService service =
                new RiskEvaluationService(repository, registry);

        RiskEvaluationResult result =
                service.evaluate(
                        transaction(),
                        new FraudEvaluationContext(null)
                );

        assertThat(result.riskScore())
                .isEqualByComparingTo("100.00");
        assertThat(result.riskLevel())
                .isEqualTo(RiskLevel.HIGH);
        assertThat(result.riskDecision())
                .isEqualTo(RiskDecision.BLOCK);
        assertThat(result.triggeredRules())
                .containsExactly("Rule One", "Rule Two");
    }

    @Test
    void shouldIgnoreInactiveRulesBecauseRepositoryReturnsOnlyActiveRules() {
        FraudRule activeRule = rule(
                "Active Rule",
                FraudRuleType.HIGH_AMOUNT,
                "1.00",
                "20.00"
        );

        FraudRuleRepository repository = mock(FraudRuleRepository.class);
        FraudRuleEvaluatorRegistry registry =
                mock(FraudRuleEvaluatorRegistry.class);
        FraudRuleEvaluator evaluator =
                mock(FraudRuleEvaluator.class);

        when(repository.findByActiveTrue())
                .thenReturn(List.of(activeRule));

        when(registry.getEvaluator(FraudRuleType.HIGH_AMOUNT))
                .thenReturn(evaluator);

        when(evaluator.evaluate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(activeRule),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(
                FraudRuleResult.notTriggered()
        );

        RiskEvaluationService service =
                new RiskEvaluationService(repository, registry);

        RiskEvaluationResult result =
                service.evaluate(
                        transaction(),
                        new FraudEvaluationContext(null)
                );

        assertThat(result.riskScore())
                .isEqualByComparingTo("0");
        assertThat(result.riskLevel())
                .isEqualTo(RiskLevel.LOW);
        assertThat(result.riskDecision())
                .isEqualTo(RiskDecision.APPROVE);
        assertThat(result.triggeredRules())
                .isEmpty();
    }

    private void assertScoreBoundary(
            String score,
            RiskLevel expectedLevel,
            RiskDecision expectedDecision
    ) {
        FraudRule rule = rule(
                "Test Rule",
                FraudRuleType.HIGH_AMOUNT,
                "1.00",
                score
        );

        FraudRuleRepository repository = mock(FraudRuleRepository.class);
        FraudRuleEvaluatorRegistry registry =
                mock(FraudRuleEvaluatorRegistry.class);
        FraudRuleEvaluator evaluator =
                mock(FraudRuleEvaluator.class);

        when(repository.findByActiveTrue())
                .thenReturn(List.of(rule));

        when(registry.getEvaluator(FraudRuleType.HIGH_AMOUNT))
                .thenReturn(evaluator);

        when(evaluator.evaluate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(rule),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(
                FraudRuleResult.triggered(
                        new BigDecimal(score),
                        "test"
                )
        );

        RiskEvaluationService service =
                new RiskEvaluationService(repository, registry);

        RiskEvaluationResult result =
                service.evaluate(
                        transaction(),
                        new FraudEvaluationContext(null)
                );

        assertThat(result.riskScore())
                .isEqualByComparingTo(score);
        assertThat(result.riskLevel())
                .isEqualTo(expectedLevel);
        assertThat(result.riskDecision())
                .isEqualTo(expectedDecision);
    }

    private FraudRule rule(
            String name,
            FraudRuleType type,
            String threshold,
            String weight
    ) {
        return new FraudRule(
                name,
                type,
                new BigDecimal(threshold),
                new BigDecimal(weight),
                true
        );
    }

    private Transaction transaction() {
        return new Transaction(
                "TEST-TXN-SCORING-001",
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