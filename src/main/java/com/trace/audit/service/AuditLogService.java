package com.trace.audit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.entity.AuditLog;
import com.trace.audit.repository.AuditLogRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog record(
            Long userId,
            AuditAction action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue,
            String ipAddress) {
        AuditLog auditLog = new AuditLog(
                userId,
                action,
                entityType,
                entityId,
                oldValue,
                newValue,
                ipAddress);

        return auditLogRepository.save(auditLog);
    }
}