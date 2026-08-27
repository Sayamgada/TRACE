package com.trace.fraud.cases;

import java.time.Instant;

import com.trace.fraud.alert.FraudAlert;
import com.trace.user.entity.User;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "fraud_cases",
        indexes = {
                @Index(
                        name = "idx_fraud_case_alert",
                        columnList = "fraud_alert_id"
                ),
                @Index(
                        name = "idx_fraud_case_assigned_analyst",
                        columnList = "assigned_analyst_id"
                ),
                @Index(
                        name = "idx_fraud_case_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_fraud_case_created_at",
                        columnList = "created_at"
                )
        }
)
public class FraudCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "fraud_alert_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_fraud_case_alert")
    )
    private FraudAlert fraudAlert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_analyst_id",
            foreignKey = @ForeignKey(name = "fk_fraud_case_analyst")
    )
    private User assignedAnalyst;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FraudCaseStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant resolvedAt;

    protected FraudCase() {
    }

    public FraudCase(
            FraudAlert fraudAlert,
            FraudCaseStatus status
    ) {
        this.fraudAlert = fraudAlert;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public FraudAlert getFraudAlert() {
        return fraudAlert;
    }

    public User getAssignedAnalyst() {
        return assignedAnalyst;
    }

    public FraudCaseStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void assignAnalyst(User analyst) {
        this.assignedAnalyst = analyst;

        if (this.status == FraudCaseStatus.OPEN) {
            this.status = FraudCaseStatus.ASSIGNED;
        }
    }

    public void setStatus(FraudCaseStatus status) {
        this.status = status;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}