package com.trace.risk.rule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trace.risk.rule.FraudRuleService;
import com.trace.risk.rule.dto.FraudRuleResponse;
import com.trace.risk.rule.dto.UpdateFraudRuleRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fraud/rules")
@Validated
public class FraudRuleController {

    private final FraudRuleService fraudRuleService;

    public FraudRuleController(FraudRuleService fraudRuleService) {
        this.fraudRuleService = fraudRuleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{ruleId}")
    public ResponseEntity<FraudRuleResponse> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody UpdateFraudRuleRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                FraudRuleResponse.from(
                        fraudRuleService.updateRule(
                                ruleId,
                                request.ruleName(),
                                request.ruleType(),
                                request.threshold(),
                                request.weight(),
                                request.active(),
                                authentication)));
    }
}