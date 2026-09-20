package com.trace.audit.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
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

import com.trace.audit.annotation.Auditable;
import com.trace.audit.entity.AuditAction;
import com.trace.audit.entity.AuditLog;
import com.trace.audit.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private AuditAspect auditAspect;

    @BeforeEach
    void setUp() {
        auditAspect = new AuditAspect(auditLogService);
    }

    @Test
    void shouldCreateAuditLogAfterAnnotatedMethodExecutes()
            throws Throwable {

        when(joinPoint.getSignature())
                .thenReturn(methodSignature);

        when(methodSignature.getMethod())
                .thenReturn(TestService.class.getMethod(
                        "createTransaction",
                        Long.class,
                        String.class));

        when(methodSignature.getParameterNames())
                .thenReturn(new String[] { "transactionId", "status" });

        when(joinPoint.getArgs())
                .thenReturn(new Object[] { 100L, "CREATED" });

        when(joinPoint.proceed())
                .thenReturn("success");

        Object result = auditAspect.audit(joinPoint);

        assertThat(result).isEqualTo("success");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogService).record(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any());
    }

    static class TestService {

        @Auditable(action = AuditAction.TRANSACTION_CREATED, entityType = "Transaction", entityIdParam = "transactionId", newValueParam = "status")
        public String createTransaction(
                Long transactionId,
                String status) {
            return "success";
        }
    }
}