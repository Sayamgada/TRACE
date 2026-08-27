package com.trace.risk.rule;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class FraudRuleRepositoryTest {

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @Test
    void shouldPersistAndFindActiveFraudRule() {
        FraudRule rule = new FraudRule(
                "High Amount",
                FraudRuleType.HIGH_AMOUNT,
                new BigDecimal("50000.00"),
                new BigDecimal("30.00"),
                true
        );

        FraudRule savedRule = fraudRuleRepository.save(rule);

        fraudRuleRepository.flush();

        assertThat(savedRule.getId()).isNotNull();
        assertThat(savedRule.getRuleName())
                .isEqualTo("High Amount");
        assertThat(savedRule.getRuleType())
                .isEqualTo(FraudRuleType.HIGH_AMOUNT);
        assertThat(savedRule.getThreshold())
                .isEqualByComparingTo("50000.00");
        assertThat(savedRule.getWeight())
                .isEqualByComparingTo("30.00");
        assertThat(savedRule.isActive()).isTrue();

        List<FraudRule> activeRules =
                fraudRuleRepository.findByActiveTrue();

        assertThat(activeRules)
                .hasSize(1)
                .first()
                .extracting(FraudRule::getRuleName)
                .isEqualTo("High Amount");
    }
}