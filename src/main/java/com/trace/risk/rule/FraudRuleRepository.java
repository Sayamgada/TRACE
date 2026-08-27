package com.trace.risk.rule;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {

    Optional<FraudRule> findByRuleName(String ruleName);

    boolean existsByRuleName(String ruleName);

    List<FraudRule> findByActiveTrue();

    List<FraudRule> findByRuleTypeAndActiveTrue(FraudRuleType ruleType);
}