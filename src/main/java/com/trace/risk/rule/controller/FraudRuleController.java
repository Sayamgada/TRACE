
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/fraud-rules")
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Fraud Rules", description = "Administrative fraud-risk rule management operations")
@SecurityRequirement(name = "bearerAuth")
public class FraudRuleController {

        private final FraudRuleService fraudRuleService;

        public FraudRuleController(FraudRuleService fraudRuleService) {
                this.fraudRuleService = fraudRuleService;
        }

        @Operation(summary = "List fraud rules", description = "Returns all configured fraud-risk rules.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud rules retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Admin role required")
        })
        @GetMapping
        public ResponseEntity<List<FraudRuleResponse>> getRules() {
                return ResponseEntity.ok(
                                fraudRuleService.getRules()
                                                .stream()
                                                .map(FraudRuleResponse::from)
                                                .toList());
        }

        @Operation(summary = "Create a fraud rule", description = "Creates a new configurable fraud-risk rule.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Fraud rule created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid fraud rule request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Admin role required")
        })
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

        @Operation(summary = "Update a fraud rule", description = "Updates the configuration of an existing fraud-risk rule.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud rule updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid fraud rule request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud rule not found")
        })
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

        @Operation(summary = "Toggle a fraud rule", description = "Toggles the active state of an existing fraud-risk rule.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud rule state toggled successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud rule not found")
        })
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