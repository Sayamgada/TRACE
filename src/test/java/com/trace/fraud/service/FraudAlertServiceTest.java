package com.trace.fraud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertRepository;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.risk.scoring.RiskDecision;
import com.trace.risk.scoring.RiskEvaluationResult;
import com.trace.risk.scoring.RiskLevel;
import com.trace.transaction.entity.Transaction;

@ExtendWith(MockitoExtension.class)
class FraudAlertServiceTest {

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private Transaction transaction;

    private FraudAlertService fraudAlertService;

    @BeforeEach
    void setUp() {
        fraudAlertService =
                new FraudAlertService(fraudAlertRepository);
    }

    @Test
    void shouldCreateHighSeverityAlertForHighRiskTransaction() {

        when(transaction.getId()).thenReturn(100L);
        when(fraudAlertRepository.findByTransactionId(100L))
                .thenReturn(Optional.empty());

        RiskEvaluationResult result =
                new RiskEvaluationResult(
                        new BigDecimal("85.00"),
                        RiskLevel.HIGH,
                        RiskDecision.BLOCK,
                        List.of(
                                "HIGH_AMOUNT",
                                "NEW_DEVICE"
                        )
                );

        FraudAlert savedAlert = new FraudAlert(
                transaction,
                new BigDecimal("85.00"),
                FraudAlertSeverity.HIGH,
                FraudAlertStatus.OPEN,
                "HIGH_AMOUNT; NEW_DEVICE"
        );

        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenReturn(savedAlert);

        FraudAlert alert =
                fraudAlertService.createAlert(
                        transaction,
                        result
                );

        assertThat(alert.getRiskScore())
                .isEqualByComparingTo("85.00");

        assertThat(alert.getSeverity())
                .isEqualTo(FraudAlertSeverity.HIGH);

        assertThat(alert.getStatus())
                .isEqualTo(FraudAlertStatus.OPEN);

        assertThat(alert.getReasons())
                .isEqualTo("HIGH_AMOUNT; NEW_DEVICE");

        verify(fraudAlertRepository)
                .save(any(FraudAlert.class));
    }

    @Test
    void shouldCreateMediumSeverityAlertForMediumRiskTransaction() {

        when(transaction.getId()).thenReturn(101L);
        when(fraudAlertRepository.findByTransactionId(101L))
                .thenReturn(Optional.empty());

        RiskEvaluationResult result =
                new RiskEvaluationResult(
                        new BigDecimal("45.00"),
                        RiskLevel.MEDIUM,
                        RiskDecision.REVIEW,
                        List.of("TRANSACTION_FREQUENCY")
                );

        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FraudAlert alert =
                fraudAlertService.createAlert(
                        transaction,
                        result
                );

        assertThat(alert.getSeverity())
                .isEqualTo(FraudAlertSeverity.MEDIUM);

        assertThat(alert.getStatus())
                .isEqualTo(FraudAlertStatus.OPEN);

        assertThat(alert.getReasons())
                .isEqualTo("TRANSACTION_FREQUENCY");
    }

    @Test
    void shouldCreateCriticalAlertForRiskScoreAtLeastNinety() {

        when(transaction.getId()).thenReturn(102L);
        when(fraudAlertRepository.findByTransactionId(102L))
                .thenReturn(Optional.empty());

        RiskEvaluationResult result =
                new RiskEvaluationResult(
                        new BigDecimal("95.00"),
                        RiskLevel.HIGH,
                        RiskDecision.BLOCK,
                        List.of("HIGH_AMOUNT")
                );

        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FraudAlert alert =
                fraudAlertService.createAlert(
                        transaction,
                        result
                );

        assertThat(alert.getSeverity())
                .isEqualTo(FraudAlertSeverity.CRITICAL);
    }

    @Test
    void shouldReturnExistingAlertInsteadOfCreatingDuplicate() {

        when(transaction.getId()).thenReturn(103L);

        FraudAlert existingAlert = new FraudAlert(
                transaction,
                new BigDecimal("80.00"),
                FraudAlertSeverity.HIGH,
                FraudAlertStatus.OPEN,
                "HIGH_AMOUNT"
        );

        when(fraudAlertRepository.findByTransactionId(103L))
                .thenReturn(Optional.of(existingAlert));

        RiskEvaluationResult result =
                new RiskEvaluationResult(
                        new BigDecimal("85.00"),
                        RiskLevel.HIGH,
                        RiskDecision.BLOCK,
                        List.of("NEW_DEVICE")
                );

        FraudAlert resultAlert =
                fraudAlertService.createAlert(
                        transaction,
                        result
                );

        assertThat(resultAlert)
                .isSameAs(existingAlert);

        verify(fraudAlertRepository, never())
                .save(any(FraudAlert.class));
    }

    @Test
    void shouldUseDefaultReasonWhenNoRulesWereTriggered() {

        when(transaction.getId()).thenReturn(104L);
        when(fraudAlertRepository.findByTransactionId(104L))
                .thenReturn(Optional.empty());

        RiskEvaluationResult result =
                new RiskEvaluationResult(
                        new BigDecimal("40.00"),
                        RiskLevel.MEDIUM,
                        RiskDecision.REVIEW,
                        List.of()
                );

        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FraudAlert alert =
                fraudAlertService.createAlert(
                        transaction,
                        result
                );

        assertThat(alert.getReasons())
                .isEqualTo("Risk conditions triggered");
    }
}
