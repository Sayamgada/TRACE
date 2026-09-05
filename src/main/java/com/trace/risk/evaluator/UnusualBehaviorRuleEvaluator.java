package com.trace.risk.evaluator;

import com.trace.risk.rule.FraudRule;
import com.trace.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class UnusualBehaviorRuleEvaluator implements FraudRuleEvaluator {

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRule rule,
            FraudEvaluationContext context
    ) {
        BigDecimal currentAmount = transaction.getAmount();

        if (currentAmount == null) {
            return FraudRuleResult.notTriggered();
        }

        List<BigDecimal> historicalAmounts =
                context.historicalTransactionAmounts()
                        .stream()
                        .filter(amount -> amount != null)
                        .filter(amount -> amount.signum() >= 0)
                        .toList();

        if (historicalAmounts.isEmpty()) {
            return FraudRuleResult.notTriggered();
        }

        BigDecimal total = historicalAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(
                BigDecimal.valueOf(historicalAmounts.size()),
                2,
                RoundingMode.HALF_UP
        );

        if (average.signum() == 0) {
            return FraudRuleResult.notTriggered();
        }

        BigDecimal threshold = rule.getThreshold();

        if (currentAmount.compareTo(
                average.multiply(threshold)
        ) > 0) {
            return FraudRuleResult.triggered(
                    rule.getWeight(),
                    "Transaction amount is significantly above the customer's historical average behavior."
            );
        }

        return FraudRuleResult.notTriggered();
    }
}