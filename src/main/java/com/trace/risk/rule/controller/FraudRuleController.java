package com.trace.risk.rule.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trace.risk.rule.FraudRuleService;
import com.trace.risk.rule.dto.CreateFraudRuleRequest;
import com.trace.risk.rule.dto.FraudRuleResponse;
import com.trace.risk.rule.dto.UpdateFraudRuleRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/fraud-rules")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class FraudRuleController {

    private final FraudRuleService fraudRuleService;

    public FraudRuleController(FraudRuleService fraudRuleService) {
        this.fraudRuleService = fraudRuleService;
    }

    @GetMapping
    public ResponseEntity<List<FraudRuleResponse>> getRules() {
        return ResponseEntity.ok(
                fraudRuleService.getRules()
                        .stream()
                        .map(FraudRuleResponse::from)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<FraudRuleResponse> createRule(
            @Valid @RequestBody CreateFraudRuleRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FraudRuleResponse.from(
                        fraudRuleService.createRule(
                                request.ruleName(),
                                request.ruleType(),
                                request.threshold(),
                                request.weight(),
                                request.active(),
                                authentication)));
    }

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

    @PatchMapping("/{ruleId}/toggle")
    public ResponseEntity<FraudRuleResponse> toggleRule(
            @PathVariable Long ruleId,
            Authentication authentication) {

        return ResponseEntity.ok(
                FraudRuleResponse.from(
                        fraudRuleService.toggleRule(
                                ruleId,
                                authentication)));
    }
}