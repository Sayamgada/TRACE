package com.trace.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.trace.audit.entity.AuditLog;

public interface AuditLogRepository
                extends JpaRepository<AuditLog, Long>,
                JpaSpecificationExecutor<AuditLog> {
}