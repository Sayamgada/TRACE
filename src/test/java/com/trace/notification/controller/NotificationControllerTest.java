package com.trace.notification.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.trace.common.exception.GlobalExceptionHandler;
import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.fraud.cases.FraudCase;
import com.trace.fraud.cases.FraudCaseStatus;
import com.trace.notification.Notification;
import com.trace.notification.NotificationService;
import com.trace.notification.NotificationType;
import com.trace.transaction.entity.Transaction;
import com.trace.user.entity.User;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = standaloneSetup(
                new NotificationController(notificationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldReturnPaginatedNotifications() throws Exception {

        Notification notification = createNotification(
                1L,
                NotificationType.TRANSACTION_FLAGGED,
                "Transaction flagged",
                "Your transaction has been flagged.",
                100L,
                null,
                false);

        Page<Notification> page = new PageImpl<>(
                List.of(notification),
                PageRequest.of(0, 10),
                1);

        when(notificationService.getMyNotifications(
                eq("customer@test.com"),
                any(Pageable.class)))
                .thenReturn(page);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "customer@test.com",
                null);

        mockMvc.perform(
                get("/api/notifications")
                        .principal(authentication)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].type")
                        .value("TRANSACTION_FLAGGED"))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Transaction flagged"))
                .andExpect(jsonPath("$.content[0].message")
                        .value("Your transaction has been flagged."))
                .andExpect(jsonPath("$.content[0].transactionId")
                        .value(100))
                .andExpect(jsonPath("$.content[0].fraudCaseId")
                        .doesNotExist())
                .andExpect(jsonPath("$.content[0].read")
                        .value(false))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(notificationService).getMyNotifications(
                eq("customer@test.com"),
                any(Pageable.class));
    }

    @Test
    void shouldReturnNotificationsFilteredByReadState() throws Exception {

        Notification notification = createNotification(
                2L,
                NotificationType.TRANSACTION_BLOCKED,
                "Transaction blocked",
                "Your transaction has been blocked.",
                200L,
                null,
                true);

        Page<Notification> page = new PageImpl<>(
                List.of(notification),
                PageRequest.of(0, 10),
                1);

        when(notificationService.getMyNotificationsByReadState(
                eq("customer@test.com"),
                eq(true),
                any(Pageable.class)))
                .thenReturn(page);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "customer@test.com",
                null);

        mockMvc.perform(
                get("/api/notifications")
                        .principal(authentication)
                        .param("read", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type")
                        .value("TRANSACTION_BLOCKED"))
                .andExpect(jsonPath("$.content[0].read")
                        .value(true));

        verify(notificationService).getMyNotificationsByReadState(
                eq("customer@test.com"),
                eq(true),
                any(Pageable.class));
    }

    @Test
    void shouldReturnUnreadNotificationCount() throws Exception {

        when(notificationService.countUnreadNotifications(
                "customer@test.com"))
                .thenReturn(4L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "customer@test.com",
                null);

        mockMvc.perform(
                get("/api/notifications/unread/count")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4));

        verify(notificationService)
                .countUnreadNotifications("customer@test.com");
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {

        Notification notification = createNotification(
                3L,
                NotificationType.FRAUD_CASE,
                "Fraud investigation opened",
                "A fraud investigation has been opened.",
                300L,
                30L,
                true);

        when(notificationService.markAsRead(
                3L,
                "customer@test.com"))
                .thenReturn(notification);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "customer@test.com",
                null);

        mockMvc.perform(
                patch("/api/notifications/3/read")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.type")
                        .value("FRAUD_CASE"))
                .andExpect(jsonPath("$.transactionId")
                        .value(300))
                .andExpect(jsonPath("$.fraudCaseId")
                        .value(30))
                .andExpect(jsonPath("$.read").value(true));

        verify(notificationService)
                .markAsRead(3L, "customer@test.com");
    }

    @Test
    void shouldReturn404WhenNotificationDoesNotExist() throws Exception {

        when(notificationService.markAsRead(
                999L,
                "customer@test.com"))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Notification not found: 999"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "customer@test.com",
                null);

        mockMvc.perform(
                patch("/api/notifications/999/read")
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Notification not found: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/notifications/999/read"));
    }

    private Notification createNotification(
            Long notificationId,
            NotificationType type,
            String title,
            String message,
            Long transactionId,
            Long fraudCaseId,
            boolean read) {

        User recipient = org.mockito.Mockito.mock(User.class);

        Transaction transaction = org.mockito.Mockito.mock(Transaction.class);

        when(transaction.getId())
                .thenReturn(transactionId);

        FraudCase fraudCase = null;

        if (fraudCaseId != null) {
            FraudAlert alert = new FraudAlert(
                    transaction,
                    new BigDecimal("85"),
                    FraudAlertSeverity.HIGH,
                    FraudAlertStatus.OPEN,
                    "Suspicious transaction");

            setId(alert, 1000L + fraudCaseId);

            fraudCase = new FraudCase(
                    alert,
                    FraudCaseStatus.OPEN);

            setId(fraudCase, fraudCaseId);
        }

        Notification notification = new Notification(
                recipient,
                type,
                title,
                message,
                transaction,
                fraudCase);

        setId(notification, notificationId);

        if (read) {
            notification.markAsRead();
        }

        return notification;
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unable to set test id",
                    exception);
        }
    }
}