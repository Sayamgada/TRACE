package com.trace.risk.context;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.trace.risk.evaluator.FraudEvaluationContext;
import com.trace.transaction.entity.Transaction;
import com.trace.transaction.repository.TransactionRepository;

@Component
public class FraudEvaluationContextFactory {

    private static final Duration FREQUENCY_WINDOW =
            Duration.ofMinutes(2);

    private static final Duration BEHAVIOR_HISTORY_WINDOW =
            Duration.ofDays(90);

    private final TransactionRepository transactionRepository;

    public FraudEvaluationContextFactory(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public FraudEvaluationContext create(
            Transaction transaction
    ) {
        Instant evaluationTime =
                transaction.getCreatedAt();

        if (evaluationTime == null) {
            return new FraudEvaluationContext(null);
        }

        Long senderAccountId =
                transaction.getSenderAccount().getId();

        Long transactionId =
                transaction.getId();

        Instant frequencyWindowStart =
                evaluationTime.minus(FREQUENCY_WINDOW);

        Instant behaviorHistoryStart =
                evaluationTime.minus(BEHAVIOR_HISTORY_WINDOW);

        List<Transaction> frequencyHistory =
                transactionRepository
                        .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                                senderAccountId,
                                transactionId,
                                frequencyWindowStart,
                                evaluationTime
                        );

        List<Transaction> behaviorHistory =
                transactionRepository
                        .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                                senderAccountId,
                                transactionId,
                                behaviorHistoryStart,
                                evaluationTime
                        );

        List<Instant> recentTransactionTimes =
                frequencyHistory.stream()
                        .map(Transaction::getCreatedAt)
                        .toList();

        List<BigDecimal> historicalTransactionAmounts =
                behaviorHistory.stream()
                        .map(Transaction::getAmount)
                        .toList();

        return new FraudEvaluationContext(
                evaluationTime,
                recentTransactionTimes,
                historicalTransactionAmounts
        );
    }
}