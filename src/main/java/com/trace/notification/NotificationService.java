package com.trace.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.cases.FraudCase;
import com.trace.transaction.entity.Transaction;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            Transaction transaction,
            FraudCase fraudCase) {

        Notification notification = new Notification(
                recipient,
                type,
                title,
                message,
                transaction,
                fraudCase);

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(
            String userEmail,
            Pageable pageable) {

        User user = getUser(userEmail);

        return notificationRepository.findByRecipientId(
                user.getId(),
                pageable);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getMyNotificationsByReadState(
            String userEmail,
            boolean read,
            Pageable pageable) {

        User user = getUser(userEmail);

        return notificationRepository.findByRecipientIdAndRead(
                user.getId(),
                read,
                pageable);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(
            String userEmail) {

        User user = getUser(userEmail);

        return notificationRepository.countByRecipientIdAndRead(
                user.getId(),
                false);
    }

    @Transactional
    public Notification markAsRead(
            Long notificationId,
            String userEmail) {

        User user = getUser(userEmail);

        Notification notification = notificationRepository.findById(
                notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

        if (!notification.getRecipient()
                .getId()
                .equals(user.getId())) {

            throw new ResourceNotFoundException(
                    "Notification not found: " + notificationId);
        }

        notification.markAsRead();

        return notificationRepository.save(notification);
    }

    private User getUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }
}