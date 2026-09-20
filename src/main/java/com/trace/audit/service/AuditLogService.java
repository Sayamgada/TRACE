package com.trace.audit.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.trace.audit.dto.AuditLogResponse;
import com.trace.audit.entity.AuditAction;
import com.trace.audit.entity.AuditLog;
import com.trace.audit.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

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

                String resolvedIpAddress =
                        ipAddress != null ? ipAddress : resolveClientIpAddress();
                AuditLog auditLog = new AuditLog(
                                userId,
                                action,
                                entityType,
                                entityId,
                                oldValue,
                                newValue,
                                resolvedIpAddress);

                return auditLogRepository.save(auditLog);
        }

        @Transactional(readOnly = true)
        public Page<AuditLogResponse> search(
                        Long userId,
                        AuditAction action,
                        String entityType,
                        Long entityId,
                        Instant from,
                        Instant to,
                        Pageable pageable) {

                Specification<AuditLog> specification = (root, query, builder) -> null;

                if (userId != null) {
                        specification = specification.and(
                                        (root, query, builder) -> builder.equal(
                                                        root.get("userId"),
                                                        userId));
                }

                if (action != null) {
                        specification = specification.and(
                                        (root, query, builder) -> builder.equal(
                                                        root.get("action"),
                                                        action));
                }

                if (entityType != null && !entityType.isBlank()) {
                        specification = specification.and(
                                        (root, query, builder) -> builder.equal(
                                                        root.get("entityType"),
                                                        entityType));
                }

                if (entityId != null) {
                        specification = specification.and(
                                        (root, query, builder) -> builder.equal(
                                                        root.get("entityId"),
                                                        entityId));
                }

                if (from != null) {
                        specification = specification.and(
                                        (root, query, builder) -> builder.greaterThanOrEqualTo(
                                                        root.get("timestamp"),
                                                        from));
                }

                if (to != null) {
                        specification = specification.and(
                                        (root, query, builder) -> builder.lessThanOrEqualTo(
                                                        root.get("timestamp"),
                                                        to));
                }

                return auditLogRepository.findAll(
                                specification,
                                pageable)
                                .map(AuditLogResponse::from);
        }

        private String resolveClientIpAddress() {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                                .getRequestAttributes();

                if (attributes == null) {
                        return null;
                }

                HttpServletRequest request = attributes.getRequest();

                String forwardedFor = request.getHeader("X-Forwarded-For");

                if (forwardedFor != null && !forwardedFor.isBlank()) {
                        return forwardedFor.split(",")[0].trim();
                }

                String realIp = request.getHeader("X-Real-IP");

                if (realIp != null && !realIp.isBlank()) {
                        return realIp.trim();
                }

                return request.getRemoteAddr();
        }
}
