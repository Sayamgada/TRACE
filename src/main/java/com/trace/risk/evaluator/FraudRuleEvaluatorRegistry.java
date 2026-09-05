package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRuleType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FraudRuleEvaluatorRegistry {

    private final Map<FraudRuleType, FraudRuleEvaluator> evaluators;

    public FraudRuleEvaluatorRegistry(
            List<FraudRuleEvaluator> evaluatorList
    ) {
        Map<FraudRuleType, FraudRuleEvaluator> registry =
                new EnumMap<>(FraudRuleType.class);

        registry.put(
                FraudRuleType.HIGH_AMOUNT,
                findEvaluator(evaluatorList, HighAmountRuleEvaluator.class)
        );

        registry.put(
                FraudRuleType.TRANSACTION_FREQUENCY,
                findEvaluator(
                        evaluatorList,
                        TransactionFrequencyRuleEvaluator.class
                )
        );

        registry.put(
                FraudRuleType.UNUSUAL_BEHAVIOR,
                findEvaluator(
                        evaluatorList,
                        UnusualBehaviorRuleEvaluator.class
                )
        );

        registry.put(
                FraudRuleType.UNUSUAL_LOCATION,
                findEvaluator(
                        evaluatorList,
                        UnusualLocationRuleEvaluator.class
                )
        );

        registry.put(
                FraudRuleType.NEW_DEVICE,
                findEvaluator(
                        evaluatorList,
                        NewDeviceRuleEvaluator.class
                )
        );

        this.evaluators = Map.copyOf(registry);
    }

    public FraudRuleEvaluator getEvaluator(FraudRuleType ruleType) {
        FraudRuleEvaluator evaluator = evaluators.get(ruleType);

        if (evaluator == null) {
            throw new IllegalArgumentException(
                    "No evaluator registered for rule type: " + ruleType
            );
        }

        return evaluator;
    }

    private FraudRuleEvaluator findEvaluator(
            List<FraudRuleEvaluator> evaluatorList,
            Class<? extends FraudRuleEvaluator> evaluatorClass
    ) {
        return evaluatorList.stream()
                .filter(evaluatorClass::isInstance)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Evaluator bean not found: "
                                        + evaluatorClass.getSimpleName()
                        )
                );
    }
}