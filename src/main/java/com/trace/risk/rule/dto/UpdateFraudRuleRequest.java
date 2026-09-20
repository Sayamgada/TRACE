package com.trace.risk.rule.dto;

import java.math.BigDecimal;

import com.trace.risk.rule.FraudRuleType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFraudRuleRequest(

        @NotBlank @Size(max = 100) String ruleName,

        @NotNull FraudRuleType ruleType,

        @NotNull @DecimalMin(value = "0.00") BigDecimal threshold,

        @NotNull @DecimalMin(value = "0.00") BigDecimal weight,

        boolean active) {
}