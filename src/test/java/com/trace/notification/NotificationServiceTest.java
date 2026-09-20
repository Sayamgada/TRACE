package com.trace.notification;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.cases.FraudCase;
import com.trace.transaction.entity.Transaction;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User recipient;

    @Mock
    private User otherUser;

    @Mock
    private Transaction transaction;

    @Mock
    private FraudCase fraudCase;

    @Mock
    private Notification notification;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                userRepository);
    }

    @Test
    void shouldCreateNotification() {

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.createNotification(
                recipient,
                NotificationType.TRANSACTION_FLAGGED,
                "Transaction flagged",
                "Your transaction has been flagged.",
                transaction,
                null);

        assertThat(result.getRecipient())
                .isSameAs(recipient);

        assertThat(result.getType())
                .isEqualTo(NotificationType.TRANSACTION_FLAGGED);

        assertThat(result.getTitle())
                .isEqualTo("Transaction flagged");

        assertThat(result.getMessage())
                .isEqualTo("Your transaction has been flagged.");

        assertThat(result.getTransaction())
                .isSameAs(transaction);

        assertThat(result.getFraudCase())
                .isNull();

        assertThat(result.isRead())
                .isFalse();

        verify(notificationRepository)
                .save(any(Notification.class));
    }

    @Test
    void shouldCreateFraudCaseNotification() {

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.createNotification(
                recipient,
                NotificationType.FRAUD_CASE,
                "Fraud investigation opened",
                "A fraud investigation has been opened.",
                transaction,
                fraudCase);

        assertThat(result.getType())
                .isEqualTo(NotificationType.FRAUD_CASE);

        assertThat(result.getTransaction())
                .isSameAs(transaction);

        assertThat(result.getFraudCase())
                .isSameAs(fraudCase);

        assertThat(result.isRead())
                .isFalse();
    }

    @Test
    void shouldReturnMyNotifications() {

        String email = "customer@example.com";
        PageRequest pageable = PageRequest.of(0, 20);

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(recipient));

        Page<Notification> page = new PageImpl<>(java.util.List.of(notification));

        when(notificationRepository.findByRecipientId(
                recipient.getId(),
                pageable))
                .thenReturn(page);

        Page<Notification> result = notificationService.getMyNotifications(
                email,
                pageable);

        assertThat(result.getContent())
                .containsExactly(notification);

        verify(notificationRepository)
                .findByRecipientId(
                        recipient.getId(),
                        pageable);
    }

    @Test
    void shouldReturnNotificationsByReadState() {

        String email = "customer@example.com";
        PageRequest pageable = PageRequest.of(0, 20);

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(recipient));

        Page<Notification> page = new PageImpl<>(java.util.List.of(notification));

        when(notificationRepository.findByRecipientIdAndRead(
                recipient.getId(),
                false,
                pageable))
                .thenReturn(page);

        Page<Notification> result = notificationService.getMyNotificationsByReadState(
                email,
                false,
                pageable);

        assertThat(result.getContent())
                .containsExactly(notification);
    }

    @Test
    void shouldCountUnreadNotifications() {

        String email = "customer@example.com";

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(recipient));

        when(notificationRepository.countByRecipientIdAndRead(
                recipient.getId(),
                false))
                .thenReturn(3L);

        long result = notificationService.countUnreadNotifications(email);

        assertThat(result)
                .isEqualTo(3L);
    }

    @Test
    void shouldMarkOwnNotificationAsRead() {

        String email = "customer@example.com";

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(recipient));

        when(notificationRepository.findById(10L))
                .thenReturn(Optional.of(notification));

        when(notification.getRecipient())
                .thenReturn(recipient);

        when(recipient.getId())
                .thenReturn(1L);

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        Notification result = notificationService.markAsRead(10L, email);

        verify(notification)
                .markAsRead();

        verify(notificationRepository)
                .save(notification);

        assertThat(result)
                .isSameAs(notification);
    }

    @Test
    void shouldRejectMarkingAnotherUsersNotificationAsRead() {

        String email = "customer@example.com";

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(recipient));

        when(notificationRepository.findById(10L))
                .thenReturn(Optional.of(notification));

        when(notification.getRecipient())
                .thenReturn(otherUser);

        when(recipient.getId())
                .thenReturn(1L);

        when(otherUser.getId())
                .thenReturn(2L);

        assertThatThrownBy(() -> notificationService.markAsRead(
                10L,
                email))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notification, never())
                .markAsRead();

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void shouldRejectUnknownNotification() {

        String email = "customer@example.com";

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(recipient));

        when(notificationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(
                999L,
                email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectUnknownUser() {

        String email = "missing@example.com";

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getMyNotifications(
                email,
                PageRequest.of(0, 20)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(notificationRepository);
    }
}