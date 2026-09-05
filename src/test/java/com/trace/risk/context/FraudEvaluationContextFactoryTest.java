package com.trace.risk.context;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trace.account.entity.Account;
import com.trace.risk.evaluator.FraudEvaluationContext;
import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;
import com.trace.transaction.entity.TransactionType;
import com.trace.transaction.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class FraudEvaluationContextFactoryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private Account senderAccount;

    @Mock
    private Account receiverAccount;

    @Test
    void shouldBuildContextFromHistoricalTransactions() {
        Instant evaluationTime =
                Instant.parse("2026-09-05T10:00:00Z");

        when(senderAccount.getId())
                .thenReturn(100L);

        Transaction currentTransaction = transaction(
                10L,
                evaluationTime,
                "60000.00"
        );

        Transaction historicalTransactionOne = transaction(
                1L,
                evaluationTime.minusSeconds(30),
                "1000.00"
        );

        Transaction historicalTransactionTwo = transaction(
                2L,
                evaluationTime.minusSeconds(60),
                "2500.00"
        );

        /*
         * Frequency history:
         * Only transactions from the previous 2 minutes
         * are supplied to recentTransactionTimes.
         */
        when(transactionRepository
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofMinutes(2)),
                        evaluationTime
                ))
                .thenReturn(
                        List.of(
                                historicalTransactionOne,
                                historicalTransactionTwo
                        )
                );

        /*
         * Behavior history:
         * Historical amounts are loaded using the separate
         * 90-day behavior-history window.
         */
        when(transactionRepository
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofDays(90)),
                        evaluationTime
                ))
                .thenReturn(
                        List.of(
                                historicalTransactionOne,
                                historicalTransactionTwo
                        )
                );

        FraudEvaluationContextFactory factory =
                new FraudEvaluationContextFactory(
                        transactionRepository
                );

        FraudEvaluationContext context =
                factory.create(currentTransaction);

        assertThat(context.evaluationTime())
                .isEqualTo(evaluationTime);

        assertThat(context.recentTransactionTimes())
                .containsExactly(
                        evaluationTime.minusSeconds(30),
                        evaluationTime.minusSeconds(60)
                );

        assertThat(context.historicalTransactionAmounts())
                .containsExactly(
                        new BigDecimal("1000.00"),
                        new BigDecimal("2500.00")
                );

        assertThat(context.currentLocation())
                .isNull();

        assertThat(context.historicalLocations())
                .isEmpty();

        assertThat(context.currentDeviceId())
                .isNull();

        assertThat(context.historicalDeviceIds())
                .isEmpty();

        verify(transactionRepository)
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofMinutes(2)),
                        evaluationTime
                );

        verify(transactionRepository)
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofDays(90)),
                        evaluationTime
                );
    }

    @Test
    void shouldReturnEmptyContextWhenTransactionCreatedAtIsNull() {
        Transaction currentTransaction = transaction(
                10L,
                null,
                "60000.00"
        );

        FraudEvaluationContextFactory factory =
                new FraudEvaluationContextFactory(
                        transactionRepository
                );

        FraudEvaluationContext context =
                factory.create(currentTransaction);

        assertThat(context.evaluationTime())
                .isNull();

        assertThat(context.recentTransactionTimes())
                .isEmpty();

        assertThat(context.historicalTransactionAmounts())
                .isEmpty();
    }

    @Test
    void shouldReturnEmptyHistoricalDataWhenNoTransactionsExist() {
        Instant evaluationTime =
                Instant.parse("2026-09-05T10:00:00Z");

        when(senderAccount.getId())
                .thenReturn(100L);

        Transaction currentTransaction = transaction(
                10L,
                evaluationTime,
                "60000.00"
        );

        when(transactionRepository
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofMinutes(2)),
                        evaluationTime
                ))
                .thenReturn(List.of());

        when(transactionRepository
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofDays(90)),
                        evaluationTime
                ))
                .thenReturn(List.of());

        FraudEvaluationContextFactory factory =
                new FraudEvaluationContextFactory(
                        transactionRepository
                );

        FraudEvaluationContext context =
                factory.create(currentTransaction);

        assertThat(context.evaluationTime())
                .isEqualTo(evaluationTime);

        assertThat(context.recentTransactionTimes())
                .isEmpty();

        assertThat(context.historicalTransactionAmounts())
                .isEmpty();

        verify(transactionRepository)
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofMinutes(2)),
                        evaluationTime
                );

        verify(transactionRepository)
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofDays(90)),
                        evaluationTime
                );
    }

    @Test
    void shouldUseSeparateWindowsForFrequencyAndBehaviorHistory() {
        Instant evaluationTime =
                Instant.parse("2026-09-05T10:00:00Z");

        when(senderAccount.getId())
                .thenReturn(100L);

        Transaction currentTransaction = transaction(
                10L,
                evaluationTime,
                "60000.00"
        );

        when(transactionRepository
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofMinutes(2)),
                        evaluationTime
                ))
                .thenReturn(List.of());

        when(transactionRepository
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofDays(90)),
                        evaluationTime
                ))
                .thenReturn(
                        List.of(
                                transaction(
                                        1L,
                                        evaluationTime.minus(Duration.ofDays(30)),
                                        "1000.00"
                                )
                        )
                );

        FraudEvaluationContextFactory factory =
                new FraudEvaluationContextFactory(
                        transactionRepository
                );

        FraudEvaluationContext context =
                factory.create(currentTransaction);

        assertThat(context.recentTransactionTimes())
                .isEmpty();

        assertThat(context.historicalTransactionAmounts())
                .containsExactly(
                        new BigDecimal("1000.00")
                );

        verify(transactionRepository)
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofMinutes(2)),
                        evaluationTime
                );

        verify(transactionRepository)
                .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                        100L,
                        10L,
                        evaluationTime.minus(Duration.ofDays(90)),
                        evaluationTime
                );
    }

    private Transaction transaction(
            Long id,
            Instant createdAt,
            String amount
    ) {
        Transaction transaction = new Transaction(
                "TEST-TXN-" + id,
                senderAccount,
                receiverAccount,
                new BigDecimal(amount),
                "INR",
                TransactionType.TRANSFER,
                TransactionStatus.PENDING,
                BigDecimal.ZERO
        );

        setId(transaction, id);
        setCreatedAt(transaction, createdAt);

        return transaction;
    }

    private void setId(
            Transaction transaction,
            Long id
    ) {
        try {
            var field =
                    Transaction.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(transaction, id);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to set transaction ID for test",
                    e
            );
        }
    }

    private void setCreatedAt(
            Transaction transaction,
            Instant createdAt
    ) {
        try {
            var field =
                    Transaction.class.getDeclaredField("createdAt");

            field.setAccessible(true);
            field.set(transaction, createdAt);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to set transaction createdAt for test",
                    e
            );
        }
    }
}