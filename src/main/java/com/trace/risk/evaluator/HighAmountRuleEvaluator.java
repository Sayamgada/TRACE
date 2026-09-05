package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRuleEvaluator implements FraudRuleEvaluator {

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRule rule,
            FraudEvaluationContext context
    ) {
        BigDecimal amount = transaction.getAmount();
        BigDecimal threshold = rule.getThreshold();

        if (amount.compareTo(threshold) > 0) {
            return FraudRuleResult.triggered(
                    rule.getWeight(),
                    "Transaction amount exceeds the configured high-amount threshold."
            );
        }

        return FraudRuleResult.notTriggered();
    }
}