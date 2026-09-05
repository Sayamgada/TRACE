package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnusualLocationRuleEvaluator implements FraudRuleEvaluator {

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRule rule,
            FraudEvaluationContext context
    ) {
        String currentLocation = context.currentLocation();

        if (currentLocation == null || currentLocation.isBlank()) {
            return FraudRuleResult.notTriggered();
        }

        List<String> historicalLocations =
                context.historicalLocations()
                        .stream()
                        .filter(location -> location != null)
                        .filter(location -> !location.isBlank())
                        .toList();

        if (historicalLocations.isEmpty()) {
            return FraudRuleResult.notTriggered();
        }

        boolean knownLocation = historicalLocations.stream()
                .anyMatch(location ->
                        location.equalsIgnoreCase(currentLocation.trim())
                );

        if (!knownLocation) {
            return FraudRuleResult.triggered(
                    rule.getWeight(),
                    "Transaction location is not present in the customer's historical locations."
            );
        }

        return FraudRuleResult.notTriggered();
    }
}