package com.trace.audit.controller;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trace.audit.dto.AuditLogResponse;
import com.trace.audit.entity.AuditAction;
import com.trace.audit.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Auditable system activity search and review operations")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "Search audit logs", description = "Searches audit logs using optional user, action, entity, and timestamp filters. "
            + "Results are paginated and sorted by timestamp by default. "
            + "Accessible to auditors and administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid audit log filter or pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Auditor or admin role required")
    })
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.search(
                        userId,
                        action,
                        entityType,
                        entityId,
                        from,
                        to,
                        pageable));
    }
}