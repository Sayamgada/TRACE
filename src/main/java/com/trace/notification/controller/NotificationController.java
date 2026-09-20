package com.trace.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trace.notification.Notification;
import com.trace.notification.NotificationService;
import com.trace.notification.dto.NotificationResponse;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false) Boolean read,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Notification> notifications = read == null
                ? notificationService.getMyNotifications(
                        authentication.getName(), pageable)
                : notificationService.getMyNotificationsByReadState(
                        authentication.getName(), read, pageable);

        return ResponseEntity.ok(
                notifications.map(NotificationResponse::from));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.countUnreadNotifications(
                        authentication.getName()));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Notification notification = notificationService.markAsRead(
                notificationId,
                authentication.getName());

        return ResponseEntity.ok(
                NotificationResponse.from(notification));
    }
}