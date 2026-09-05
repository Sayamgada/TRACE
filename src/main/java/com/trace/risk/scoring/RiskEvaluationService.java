package com.trace.risk.scoring;

import com.trace.risk.evaluator.FraudEvaluationContext;
import com.trace.risk.evaluator.FraudRuleEvaluator;
import com.trace.risk.evaluator.FraudRuleEvaluatorRegistry;
import com.trace.risk.evaluator.FraudRuleResult;
import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleRepository;
import com.trace.transaction.entity.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskEvaluationService {

    private static final BigDecimal MAX_RISK_SCORE =
            new BigDecimal("100.00");

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudRuleEvaluatorRegistry evaluatorRegistry;

    public RiskEvaluationService(
            FraudRuleRepository fraudRuleRepository,
            FraudRuleEvaluatorRegistry evaluatorRegistry
    ) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.evaluatorRegistry = evaluatorRegistry;
    }

    public RiskEvaluationResult evaluate(
            Transaction transaction,
            FraudEvaluationContext context
    ) {
        List<FraudRule> activeRules =
                fraudRuleRepository.findByActiveTrue();

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> triggeredRules = new ArrayList<>();

        for (FraudRule rule : activeRules) {
            FraudRuleEvaluator evaluator =
                    evaluatorRegistry.getEvaluator(rule.getRuleType());

            FraudRuleResult result =
                    evaluator.evaluate(
                            transaction,
                            rule,
                            context
                    );

            if (result.triggered()) {
                riskScore = riskScore.add(
                        result.riskContribution()
                );

                triggeredRules.add(rule.getRuleName());
            }
        }

        riskScore = capRiskScore(riskScore);

        RiskLevel riskLevel = determineRiskLevel(riskScore);
        RiskDecision riskDecision = determineRiskDecision(riskLevel);

        return new RiskEvaluationResult(
                riskScore,
                riskLevel,
                riskDecision,
                triggeredRules
        );
    }

    private BigDecimal capRiskScore(BigDecimal riskScore) {
        if (riskScore.compareTo(MAX_RISK_SCORE) > 0) {
            return MAX_RISK_SCORE;
        }

        return riskScore;
    }

    private RiskLevel determineRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(new BigDecimal("30.00")) < 0) {
            return RiskLevel.LOW;
        }

        if (riskScore.compareTo(new BigDecimal("70.00")) < 0) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.HIGH;
    }

    private RiskDecision determineRiskDecision(
            RiskLevel riskLevel
    ) {
        return switch (riskLevel) {
            case LOW -> RiskDecision.APPROVE;
            case MEDIUM -> RiskDecision.REVIEW;
            case HIGH -> RiskDecision.BLOCK;
        };
    }
}