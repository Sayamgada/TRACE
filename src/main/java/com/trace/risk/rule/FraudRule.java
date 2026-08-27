package com.trace.risk.rule;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "fraud_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fraud_rule_name",
                        columnNames = "rule_name"
                )
        },
        indexes = {
                @Index(
                        name = "idx_fraud_rule_active",
                        columnList = "active"
                ),
                @Index(
                        name = "idx_fraud_rule_type",
                        columnList = "rule_type"
                )
        }
)
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "rule_name",
            nullable = false,
            length = 100
    )
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "rule_type",
            nullable = false,
            length = 50
    )
    private FraudRuleType ruleType;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal threshold;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal weight;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected FraudRule() {
    }

    public FraudRule(
            String ruleName,
            FraudRuleType ruleType,
            BigDecimal threshold,
            BigDecimal weight,
            boolean active
    ) {
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.threshold = threshold;
        this.weight = weight;
        this.active = active;
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

    public String getRuleName() {
        return ruleName;
    }

    public FraudRuleType getRuleType() {
        return ruleType;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setRuleType(FraudRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}