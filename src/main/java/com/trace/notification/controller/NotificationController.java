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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Customer notification retrieval and read-state management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

        private final NotificationService notificationService;

        public NotificationController(NotificationService notificationService) {
                this.notificationService = notificationService;
        }

        @Operation(summary = "Get my notifications", description = "Returns the authenticated customer's notifications. "
                        + "The optional read parameter filters notifications by read state. "
                        + "Results are paginated and sorted by creation time by default.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid notification filter or pagination parameters"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
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

        @Operation(summary = "Count unread notifications", description = "Returns the number of unread notifications belonging to the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Unread notification count retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/unread/count")
        public ResponseEntity<Long> countUnreadNotifications(
                        Authentication authentication) {

                return ResponseEntity.ok(
                                notificationService.countUnreadNotifications(
                                                authentication.getName()));
        }

        @Operation(summary = "Mark a notification as read", description = "Marks the specified notification as read for the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required or notification does not belong to the customer"),
                        @ApiResponse(responseCode = "404", description = "Notification not found")
        })
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