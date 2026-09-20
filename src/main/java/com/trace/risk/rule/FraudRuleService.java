package com.trace.risk.rule;

import java.math.BigDecimal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.audit.entity.AuditAction;
import com.trace.audit.service.AuditLogService;
import com.trace.common.exception.ResourceNotFoundException;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@Service
public class FraudRuleService {

    private final FraudRuleRepository fraudRuleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public FraudRuleService(
            FraudRuleRepository fraudRuleRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public FraudRule updateRule(
            Long ruleId,
            String ruleName,
            FraudRuleType ruleType,
            BigDecimal threshold,
            BigDecimal weight,
            boolean active,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        FraudRule rule = fraudRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fraud rule not found: " + ruleId));

        String oldValue = serializeRule(rule);

        rule.setRuleName(ruleName);
        rule.setRuleType(ruleType);
        rule.setThreshold(threshold);
        rule.setWeight(weight);
        rule.setActive(active);

        FraudRule savedRule = fraudRuleRepository.save(rule);

        String newValue = serializeRule(savedRule);

        auditLogService.record(
                user.getId(),
                AuditAction.FRAUD_RULE_UPDATED,
                "FraudRule",
                savedRule.getId(),
                oldValue,
                newValue,
                null);

        return savedRule;
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "Authentication is required");
        }

        return userRepository.findByEmailIgnoreCase(
                authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user was not found"));
    }

    private String serializeRule(FraudRule rule) {

        return "ruleName=" + rule.getRuleName()
                + ",ruleType=" + rule.getRuleType()
                + ",threshold=" + rule.getThreshold()
                + ",weight=" + rule.getWeight()
                + ",active=" + rule.isActive();
    }
}
