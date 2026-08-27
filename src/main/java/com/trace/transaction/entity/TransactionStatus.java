package com.trace.transaction.entity;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    APPROVED,
    FLAGGED,
    BLOCKED,
    FAILED,
    REVERSED
}