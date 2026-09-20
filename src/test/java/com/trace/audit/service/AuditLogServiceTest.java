package com.trace.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.entity.AuditLog;
import com.trace.audit.repository.AuditLogRepository;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogRepository);
    }

    @Test
    void shouldRecordAuditLog() {
        AuditLog saved = new AuditLog(
                42L,
                AuditAction.TRANSACTION_CREATED,
                "Transaction",
                100L,
                null,
                "{\"status\":\"CREATED\"}",
                "127.0.0.1");

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenReturn(saved);

        AuditLog result = auditLogService.record(
                42L,
                AuditAction.TRANSACTION_CREATED,
                "Transaction",
                100L,
                null,
                "{\"status\":\"CREATED\"}",
                "127.0.0.1");

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog captured = captor.getValue();

        assertThat(captured.getUserId()).isEqualTo(42L);
        assertThat(captured.getAction())
                .isEqualTo(AuditAction.TRANSACTION_CREATED);
        assertThat(captured.getEntityType())
                .isEqualTo("Transaction");
        assertThat(captured.getEntityId()).isEqualTo(100L);
        assertThat(captured.getOldValue()).isNull();
        assertThat(captured.getNewValue())
                .isEqualTo("{\"status\":\"CREATED\"}");
        assertThat(captured.getIpAddress())
                .isEqualTo("127.0.0.1");
    }
}