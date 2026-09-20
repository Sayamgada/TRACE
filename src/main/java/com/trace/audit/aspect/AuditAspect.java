package com.trace.audit.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.trace.audit.annotation.Auditable;
import com.trace.audit.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(com.trace.audit.annotation.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature())
                .getMethod();

        Auditable auditable = method.getAnnotation(Auditable.class);

        Object result = joinPoint.proceed();

        Long userId = resolveUserId();
        Long entityId = resolveLongParameter(
                joinPoint,
                auditable.entityIdParam());

        String oldValue = resolveStringParameter(
                joinPoint,
                auditable.oldValueParam());

        String newValue = resolveStringParameter(
                joinPoint,
                auditable.newValueParam());

        String ipAddress = resolveIpAddress();

        auditLogService.record(
                userId,
                auditable.action(),
                auditable.entityType(),
                entityId,
                oldValue,
                newValue,
                ipAddress);

        return result;
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        try {
            Method getUserId = principal.getClass()
                    .getMethod("getUserId");

            Object value = getUserId.invoke(principal);

            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (ReflectiveOperationException ignored) {
            // Anonymous or non-standard principals do not expose a user ID.
        }

        return null;
    }

    private Long resolveLongParameter(
            ProceedingJoinPoint joinPoint,
            String parameterName) {
        Object value = resolveParameter(joinPoint, parameterName);

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String string && !string.isBlank()) {
            try {
                return Long.valueOf(string);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String resolveStringParameter(
            ProceedingJoinPoint joinPoint,
            String parameterName) {
        Object value = resolveParameter(joinPoint, parameterName);
        return value == null ? null : value.toString();
    }

    private Object resolveParameter(
            ProceedingJoinPoint joinPoint,
            String parameterName) {
        if (parameterName == null || parameterName.isBlank()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = signature.getParameterNames();
        Object[] arguments = joinPoint.getArgs();

        if (parameterNames == null) {
            return null;
        }

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterName.equals(parameterNames[i])) {
                return arguments[i];
            }
        }

        return null;
    }

    private String resolveIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        return request.getRemoteAddr();
    }
}