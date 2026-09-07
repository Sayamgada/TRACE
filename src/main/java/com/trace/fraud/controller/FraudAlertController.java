package com.trace.fraud.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/fraud/alerts")
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    public FraudAlertController(FraudAlertService fraudAlertService) {
        this.fraudAlertService = fraudAlertService;
    }

    @GetMapping

    public Page<FraudAlertResponse> getAlerts(
            @RequestParam(name = "status", required = false) FraudAlertStatus status,
@RequestParam(name = "severity", required = false) FraudAlertSeverity severity,
            Pageable pageable
    ) {
        Page<FraudAlert> alerts;

        if (status != null && severity != null) {
            alerts = fraudAlertService.getAlertsByStatusAndSeverity(
                    status,
                    severity,
                    pageable
            );
        } else if (status != null) {
            alerts = fraudAlertService.getAlertsByStatus(status, pageable);
        } else if (severity != null) {
            alerts = fraudAlertService.getAlertsBySeverity(severity, pageable);
        } else {
            alerts = fraudAlertService.getAlerts(pageable);
        }

        return alerts.map(this::toResponse);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<FraudAlertResponse> getAlert(
            @PathVariable Long alertId
    ) {
        return ResponseEntity.ok(
                toResponse(fraudAlertService.getAlert(alertId))
        );
    }

    private FraudAlertResponse toResponse(FraudAlert alert) {
        return new FraudAlertResponse(
                alert.getId(),
                alert.getTransaction().getId(),
                alert.getRiskScore(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getReasons(),
                alert.getCreatedAt()
        );
    }
}
