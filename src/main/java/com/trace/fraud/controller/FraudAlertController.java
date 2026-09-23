package com.trace.fraud.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.fraud.alert.dto.FraudAlertResponse;
import com.trace.fraud.service.FraudAlertService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fraud/alerts")
@Tag(name = "Fraud Alerts", description = "Fraud alert monitoring and investigation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    public FraudAlertController(FraudAlertService fraudAlertService) {
        this.fraudAlertService = fraudAlertService;
    }

    @Operation(summary = "List fraud alerts", description = "Returns a paginated list of fraud alerts for fraud analysts. "
            + "Alerts can optionally be filtered by status and severity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fraud alerts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status, severity, or pagination parameter"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Fraud analyst role required")
    })
    @PreAuthorize("hasRole('FRAUD_ANALYST')")
    @GetMapping
    public Page<FraudAlertResponse> getAlerts(
            @RequestParam(name = "status", required = false) FraudAlertStatus status,
            @RequestParam(name = "severity", required = false) FraudAlertSeverity severity,
            Pageable pageable) {

        Page<FraudAlert> alerts;

        if (status != null && severity != null) {
            alerts = fraudAlertService.getAlertsByStatusAndSeverity(
                    status,
                    severity,
                    pageable);
        } else if (status != null) {
            alerts = fraudAlertService.getAlertsByStatus(status, pageable);
        } else if (severity != null) {
            alerts = fraudAlertService.getAlertsBySeverity(severity, pageable);
        } else {
            alerts = fraudAlertService.getAlerts(pageable);
        }

        return alerts.map(this::toResponse);
    }

    @Operation(summary = "Get a fraud alert", description = "Returns a specific fraud alert by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fraud alert retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
            @ApiResponse(responseCode = "404", description = "Fraud alert not found")
    })
    @PreAuthorize("hasRole('FRAUD_ANALYST')")
    @GetMapping("/{alertId}")
    public ResponseEntity<FraudAlertResponse> getAlert(
            @PathVariable Long alertId) {

        return ResponseEntity.ok(
                toResponse(fraudAlertService.getAlert(alertId)));
    }

    private FraudAlertResponse toResponse(FraudAlert alert) {
        return new FraudAlertResponse(
                alert.getId(),
                alert.getTransaction().getId(),
                alert.getRiskScore(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getReasons(),
                alert.getCreatedAt());
    }
}