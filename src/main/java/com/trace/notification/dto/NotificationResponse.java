package com.trace.notification.dto;

import java.time.Instant;

import com.trace.notification.Notification;
import com.trace.notification.NotificationType;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long transactionId,
        Long fraudCaseId,
        boolean read,
        Instant createdAt,
        Instant readAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTransaction() != null
                        ? notification.getTransaction().getId()
                        : null,
                notification.getFraudCase() != null
                        ? notification.getFraudCase().getId()
                        : null,
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt());  
    }
}