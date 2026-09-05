package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewDeviceRuleEvaluator implements FraudRuleEvaluator {

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRule rule,
            FraudEvaluationContext context
    ) {
        String currentDeviceId = context.currentDeviceId();

        if (currentDeviceId == null || currentDeviceId.isBlank()) {
            return FraudRuleResult.notTriggered();
        }

        List<String> historicalDeviceIds =
                context.historicalDeviceIds()
                        .stream()
                        .filter(deviceId -> deviceId != null)
                        .filter(deviceId -> !deviceId.isBlank())
                        .toList();

        if (historicalDeviceIds.isEmpty()) {
            return FraudRuleResult.notTriggered();
        }

        boolean knownDevice = historicalDeviceIds.stream()
                .anyMatch(deviceId ->
                        deviceId.equals(currentDeviceId.trim())
                );

        if (!knownDevice) {
            return FraudRuleResult.triggered(
                    rule.getWeight(),
                    "Transaction is being performed from a device not previously associated with the customer."
            );
        }

        return FraudRuleResult.notTriggered();
    }
}