package com.trace.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;
import com.trace.transaction.entity.TransactionType;

public record TransactionResponse(

        Long id,
        String transactionReference,
        Long senderAccountId,
        Long receiverAccountId,
        BigDecimal amount,
        String currency,
        TransactionType transactionType,
        TransactionStatus status,
        BigDecimal riskScore,
        Instant createdAt,
        Instant processedAt

) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getSenderAccount().getId(),
                transaction.getReceiverAccount().getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getRiskScore(),
                transaction.getCreatedAt(),
                transaction.getProcessedAt()
        );
    }
}
