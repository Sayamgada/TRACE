package com.trace.risk.rule;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.service.AuditLogService;
import com.trace.common.exception.ResourceNotFoundException;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FraudRuleServiceTest {

        @Mock
        private FraudRuleRepository fraudRuleRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private AuditLogService auditLogService;

        @Mock
        private Authentication authentication;

        @Mock
        private User user;

        private FraudRuleService fraudRuleService;

        @BeforeEach
        void setUp() {
                fraudRuleService = new FraudRuleService(
                                fraudRuleRepository,
                                userRepository,
                                auditLogService);
        }

        @Test
        void shouldUpdateRuleAndCreateAuditEvent() {

                Long ruleId = 10L;

                FraudRule rule = new FraudRule(
                                "HIGH_AMOUNT",
                                FraudRuleType.HIGH_AMOUNT,
                                new BigDecimal("10000.00"),
                                new BigDecimal("30.00"),
                                true);

                ReflectionTestUtils.setField(rule, "id", ruleId);

                FraudRule savedRule = rule;

                when(authentication.isAuthenticated())
                                .thenReturn(true);

                when(authentication.getName())
                                .thenReturn("admin@trace.local");

                when(userRepository.findByEmailIgnoreCase(
                                "admin@trace.local"))
                                .thenReturn(Optional.of(user));

                when(user.getId())
                                .thenReturn(42L);

                when(fraudRuleRepository.findById(ruleId))
                                .thenReturn(Optional.of(rule));

                when(fraudRuleRepository.save(rule))
                                .thenReturn(savedRule);

                FraudRule result = fraudRuleService.updateRule(
                                ruleId,
                                "HIGH_AMOUNT_UPDATED",
                                FraudRuleType.HIGH_AMOUNT,
                                new BigDecimal("15000.00"),
                                new BigDecimal("35.00"),
                                false,
                                authentication);

                assertThat(result)
                                .isSameAs(savedRule);

                assertThat(result.getRuleName())
                                .isEqualTo("HIGH_AMOUNT_UPDATED");

                assertThat(result.getThreshold())
                                .isEqualByComparingTo("15000.00");

                assertThat(result.getWeight())
                                .isEqualByComparingTo("35.00");

                assertThat(result.isActive())
                                .isFalse();

                verify(fraudRuleRepository)
                                .save(rule);

                verify(auditLogService).record(
                                eq(42L),
                                eq(AuditAction.FRAUD_RULE_UPDATED),
                                eq("FraudRule"),
                                eq(ruleId),
                                eq("ruleName=HIGH_AMOUNT,ruleType=HIGH_AMOUNT,threshold=10000.00,weight=30.00,active=true"),
                                eq("ruleName=HIGH_AMOUNT_UPDATED,ruleType=HIGH_AMOUNT,threshold=15000.00,weight=35.00,active=false"),
                                eq(null));
        }

        @Test
        void shouldRejectUpdateWhenRuleDoesNotExist() {

                Long ruleId = 999L;

                when(authentication.isAuthenticated())
                                .thenReturn(true);

                when(authentication.getName())
                                .thenReturn("admin@trace.local");

                when(userRepository.findByEmailIgnoreCase(
                                "admin@trace.local"))
                                .thenReturn(Optional.of(user));

                when(fraudRuleRepository.findById(ruleId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> fraudRuleService.updateRule(
                                ruleId,
                                "UPDATED",
                                FraudRuleType.HIGH_AMOUNT,
                                new BigDecimal("15000.00"),
                                new BigDecimal("35.00"),
                                true,
                                authentication))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Fraud rule not found: 999");

                verify(fraudRuleRepository, never())
                                .save(any(FraudRule.class));

                verify(auditLogService, never()).record(
                                any(),
                                any(AuditAction.class),
                                any(),
                                any(),
                                any(),
                                any(),
                                any());
        }

        @Test
        void shouldRejectUpdateWithoutAuthentication() {

                when(authentication.isAuthenticated())
                                .thenReturn(false);

                assertThatThrownBy(() -> fraudRuleService.updateRule(
                                10L,
                                "UPDATED",
                                FraudRuleType.HIGH_AMOUNT,
                                new BigDecimal("15000.00"),
                                new BigDecimal("35.00"),
                                true,
                                authentication))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("Authentication is required");

                verify(userRepository, never())
                                .findByEmailIgnoreCase(anyString());

                verify(fraudRuleRepository, never())
                                .findById(anyLong());

                verify(auditLogService, never()).record(
                                any(),
                                any(AuditAction.class),
                                any(),
                                any(),
                                any(),
                                any(),
                                any());
        }
}
