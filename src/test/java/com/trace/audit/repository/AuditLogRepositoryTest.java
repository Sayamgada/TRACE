package com.trace.audit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.entity.AuditLog;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldPersistAndRetrieveAuditLog() {
        AuditLog auditLog = new AuditLog(
                1L,
                AuditAction.USER_LOGIN,
                "User",
                1L,
                null,
                "{\"result\":\"SUCCESS\"}",
                "127.0.0.1"
        );

        AuditLog saved = auditLogRepository.saveAndFlush(auditLog);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTimestamp()).isNotNull();

        Optional<AuditLog> found =
                auditLogRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getAction())
                .isEqualTo(AuditAction.USER_LOGIN);
        assertThat(found.get().getEntityType())
                .isEqualTo("User");
        assertThat(found.get().getEntityId())
                .isEqualTo(1L);
        assertThat(found.get().getOldValue()).isNull();
        assertThat(found.get().getNewValue())
                .isEqualTo("{\"result\":\"SUCCESS\"}");
        assertThat(found.get().getIpAddress())
                .isEqualTo("127.0.0.1");
    }

    @Test
    void shouldPersistAllAuditActions() {
        for (AuditAction action : AuditAction.values()) {
            AuditLog auditLog = new AuditLog(
                    1L,
                    action,
                    "TestEntity",
                    1L,
                    null,
                    "{}",
                    "127.0.0.1"
            );

            AuditLog saved = auditLogRepository.saveAndFlush(auditLog);

            assertThat(saved.getAction()).isEqualTo(action);
        }

        assertThat(auditLogRepository.count())
                .isEqualTo(AuditAction.values().length);
    }
}