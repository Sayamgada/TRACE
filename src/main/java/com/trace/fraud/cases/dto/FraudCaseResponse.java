package com.trace.fraud.cases.dto;

import java.time.Instant;

import com.trace.fraud.cases.FraudCase;
import com.trace.fraud.cases.FraudCaseStatus;

public record FraudCaseResponse(
        Long id,
        Long fraudAlertId,
        Long assignedAnalystId,
        String assignedAnalystEmail,
        FraudCaseStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt) {

    public static FraudCaseResponse from(FraudCase fraudCase) {
        return new FraudCaseResponse(
                fraudCase.getId(),
                fraudCase.getFraudAlert().getId(),
                fraudCase.getAssignedAnalyst() != null
                        ? fraudCase.getAssignedAnalyst().getId()
                        : null,
                fraudCase.getAssignedAnalyst() != null
                        ? fraudCase.getAssignedAnalyst().getEmail()
                        : null,
                fraudCase.getStatus(),
                fraudCase.getCreatedAt(),
                fraudCase.getUpdatedAt(),
                fraudCase.getResolvedAt());
    }
}