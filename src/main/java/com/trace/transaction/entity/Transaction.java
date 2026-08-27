package com.trace.transaction.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.trace.account.entity.Account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transaction_reference",
                        columnNames = "transaction_reference"
                )
        },
        indexes = {
                @Index(
                        name = "idx_transaction_sender_account",
                        columnList = "sender_account_id"
                ),
                @Index(
                        name = "idx_transaction_receiver_account",
                        columnList = "receiver_account_id"
                ),
                @Index(
                        name = "idx_transaction_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_transaction_created_at",
                        columnList = "created_at"
                )
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_reference",
            nullable = false,
            length = 50
    )
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sender_account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transaction_sender_account")
    )
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "receiver_account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transaction_receiver_account")
    )
    private Account receiverAccount;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal riskScore;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant processedAt;

    protected Transaction() {
    }

    public Transaction(
            String transactionReference,
            Account senderAccount,
            Account receiverAccount,
            BigDecimal amount,
            String currency,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal riskScore
    ) {
        this.transactionReference = transactionReference;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.status = status;
        this.riskScore = riskScore;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Account getSenderAccount() {
        return senderAccount;
    }

    public Account getReceiverAccount() {
        return receiverAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}