package com.trace.fraud.alert;

import java.math.BigDecimal;
import java.time.Instant;

import com.trace.transaction.entity.Transaction;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "fraud_alerts",
        indexes = {
                @Index(
                        name = "idx_fraud_alert_transaction",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_fraud_alert_severity",
                        columnList = "severity"
                ),
                @Index(
                        name = "idx_fraud_alert_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_fraud_alert_created_at",
                        columnList = "created_at"
                )
        }
)
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "transaction_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_fraud_alert_transaction")
    )
    private Transaction transaction;

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FraudAlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FraudAlertStatus status;

    @Column(nullable = false, length = 2000)
    private String reasons;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected FraudAlert() {
    }

    public FraudAlert(
            Transaction transaction,
            BigDecimal riskScore,
            FraudAlertSeverity severity,
            FraudAlertStatus status,
            String reasons
    ) {
        this.transaction = transaction;
        this.riskScore = riskScore;
        this.severity = severity;
        this.status = status;
        this.reasons = reasons;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public FraudAlertSeverity getSeverity() {
        return severity;
    }

    public FraudAlertStatus getStatus() {
        return status;
    }

    public String getReasons() {
        return reasons;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public void setSeverity(FraudAlertSeverity severity) {
        this.severity = severity;
    }

    public void setStatus(FraudAlertStatus status) {
        this.status = status;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }
}