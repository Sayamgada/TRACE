package com.trace.audit.entity;

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
import jakarta.persistence.Table;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_log_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_audit_log_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_audit_log_entity",
                        columnList = "entity_type, entity_id"
                ),
                @Index(
                        name = "idx_audit_log_timestamp",
                        columnList = "timestamp"
                )
        }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(
            name = "entity_type",
            nullable = false,
            length = 100
    )
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    protected AuditLog() {
    }

    public AuditLog(
            Long userId,
            AuditAction action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue,
            String ipAddress
    ) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
    }

    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
