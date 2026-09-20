package com.trace.audit.dto;

import java.time.Instant;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.entity.AuditLog;

public record AuditLogResponse(
        Long id,
        Long userId,
        AuditAction action,
        String entityType,
        Long entityId,
        String oldValue,
        String newValue,
        Instant timestamp,
        String ipAddress) {

    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getTimestamp(),
                auditLog.getIpAddress());
    }
}
