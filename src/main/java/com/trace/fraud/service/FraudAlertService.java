package com.trace.fraud.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertRepository;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.risk.scoring.RiskEvaluationResult;
import com.trace.risk.scoring.RiskLevel;
import com.trace.transaction.entity.Transaction;

@Service
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;

    public FraudAlertService(
            FraudAlertRepository fraudAlertRepository
    ) {
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @Transactional
    public FraudAlert createAlert(
            Transaction transaction,
            RiskEvaluationResult riskResult
    ) {
        if (fraudAlertRepository
                .findByTransactionId(transaction.getId())
                .isPresent()) {

            return fraudAlertRepository
                    .findByTransactionId(transaction.getId())
                    .orElseThrow();
        }

        FraudAlertSeverity severity =
                determineSeverity(riskResult);

        String reasons =
                buildReasons(riskResult.triggeredRules());

        FraudAlert alert = new FraudAlert(
                transaction,
                riskResult.riskScore(),
                severity,
                FraudAlertStatus.OPEN,
                reasons
        );

        return fraudAlertRepository.save(alert);
    }

    private FraudAlertSeverity determineSeverity(
            RiskEvaluationResult riskResult
    ) {
        if (riskResult.riskLevel() == RiskLevel.HIGH) {

            BigDecimal score = riskResult.riskScore();

            if (score.compareTo(new BigDecimal("90")) >= 0) {
                return FraudAlertSeverity.CRITICAL;
            }

            return FraudAlertSeverity.HIGH;
        }

        if (riskResult.riskLevel() == RiskLevel.MEDIUM) {
            return FraudAlertSeverity.MEDIUM;
        }

        return FraudAlertSeverity.LOW;
    }

    private String buildReasons(
            List<String> triggeredRules
    ) {
        if (triggeredRules == null || triggeredRules.isEmpty()) {
            return "Risk conditions triggered";
        }

        return triggeredRules.stream()
                .filter(reason -> reason != null && !reason.isBlank())
                .collect(Collectors.joining("; "));
    }
}
