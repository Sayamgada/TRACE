package com.trace.audit.service;

import java.time.Instant;
import java.util.List;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.trace.audit.dto.AuditLogResponse;
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

        @Test
        void shouldSearchAuditLogsWithFiltersAndPagination() {

                AuditLog auditLog = new AuditLog(
                                42L,
                                AuditAction.USER_LOGIN,
                                "User",
                                42L,
                                null,
                                "LOGIN_SUCCESS",
                                "127.0.0.1");

                Page<AuditLog> page = new PageImpl<>(List.of(auditLog));

                Pageable pageable = PageRequest.of(
                                0,
                                20,
                                Sort.by(Sort.Direction.DESC, "timestamp"));

                when(auditLogRepository.findAll(
                                any(Specification.class),
                                org.mockito.ArgumentMatchers.eq(pageable)))
                                .thenReturn(page);

                Page<AuditLogResponse> result = auditLogService.search(
                                42L,
                                AuditAction.USER_LOGIN,
                                "User",
                                42L,
                                null,
                                null,
                                pageable);

                assertThat(result.getContent())
                                .hasSize(1);

                AuditLogResponse response = result.getContent().get(0);

                assertThat(response.userId())
                                .isEqualTo(42L);

                assertThat(response.action())
                                .isEqualTo(AuditAction.USER_LOGIN);

                assertThat(response.entityType())
                                .isEqualTo("User");

                assertThat(response.entityId())
                                .isEqualTo(42L);

                assertThat(response.newValue())
                                .isEqualTo("LOGIN_SUCCESS");

                assertThat(response.ipAddress())
                                .isEqualTo("127.0.0.1");

                assertThat(result.getContent())
                                .hasSize(1);

                verify(auditLogRepository)
                                .findAll(
                                                any(Specification.class),
                                                org.mockito.ArgumentMatchers.eq(pageable));
        }

        @Test
        void shouldSearchAuditLogsWithTimestampRange() {

                Instant from = Instant.parse(
                                "2026-09-20T00:00:00Z");

                Instant to = Instant.parse(
                                "2026-09-20T23:59:59Z");

                Pageable pageable = PageRequest.of(0, 10);

                Page<AuditLog> page = new PageImpl<>(List.of());

                when(auditLogRepository.findAll(
                                any(Specification.class),
                                org.mockito.ArgumentMatchers.eq(pageable)))
                                .thenReturn(page);

                Page<AuditLogResponse> result = auditLogService.search(
                                null,
                                null,
                                null,
                                null,
                                from,
                                to,
                                pageable);

                assertThat(result.getContent())
                                .isEmpty();

                verify(auditLogRepository)
                                .findAll(
                                                any(Specification.class),
                                                org.mockito.ArgumentMatchers.eq(pageable));
        }

        @Test
        void shouldSearchAuditLogsWithoutFilters() {

                Pageable pageable = PageRequest.of(0, 20);

                AuditLog auditLog = new AuditLog(
                                null,
                                AuditAction.TRANSACTION_CREATED,
                                "Transaction",
                                100L,
                                null,
                                "PENDING",
                                null);

                Page<AuditLog> page = new PageImpl<>(List.of(auditLog));

                when(auditLogRepository.findAll(
                                any(Specification.class),
                                org.mockito.ArgumentMatchers.eq(pageable)))
                                .thenReturn(page);

                Page<AuditLogResponse> result = auditLogService.search(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                pageable);

                assertThat(result.getContent())
                                .hasSize(1);

                assertThat(result.getContent().get(0).action())
                                .isEqualTo(AuditAction.TRANSACTION_CREATED);

                assertThat(result.getContent().get(0).entityId())
                                .isEqualTo(100L);
        }

}