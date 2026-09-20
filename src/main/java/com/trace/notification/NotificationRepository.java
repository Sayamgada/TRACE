package com.trace.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientId(
            Long recipientId,
            Pageable pageable);

    Page<Notification> findByRecipientIdAndRead(
            Long recipientId,
            boolean read,
            Pageable pageable);

    long countByRecipientIdAndRead(
            Long recipientId,
            boolean read);
}