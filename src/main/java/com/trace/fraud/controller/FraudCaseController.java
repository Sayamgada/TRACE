package com.trace.fraud.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trace.fraud.cases.FraudCase;
import com.trace.fraud.cases.FraudCaseStatus;
import com.trace.fraud.cases.InvestigationNote;
import com.trace.fraud.cases.dto.AssignFraudCaseRequest;
import com.trace.fraud.cases.dto.CreateInvestigationNoteRequest;
import com.trace.fraud.cases.dto.FraudCaseResponse;
import com.trace.fraud.cases.dto.InvestigationNoteResponse;
import com.trace.fraud.service.FraudCaseService;
import com.trace.fraud.service.InvestigationNoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fraud/cases")
@Validated
@PreAuthorize("hasRole('FRAUD_ANALYST')")
@Tag(name = "Fraud Cases", description = "Fraud investigation case management and investigation notes")
@SecurityRequirement(name = "bearerAuth")
public class FraudCaseController {

        private final FraudCaseService fraudCaseService;
        private final InvestigationNoteService investigationNoteService;

        public FraudCaseController(FraudCaseService fraudCaseService,
                        InvestigationNoteService investigationNoteService) {
                this.fraudCaseService = fraudCaseService;
                this.investigationNoteService = investigationNoteService;
        }

        @Operation(summary = "Create a fraud case from an alert", description = "Creates a fraud investigation case from the specified fraud alert.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Fraud case created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid case creation request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud alert not found")
        })
        @PostMapping("/from-alert/{alertId}")
        public ResponseEntity<FraudCaseResponse> createCase(
                        @PathVariable Long alertId) {

                FraudCase fraudCase = fraudCaseService.createCase(alertId);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(FraudCaseResponse.from(fraudCase));
        }

        @Operation(summary = "List fraud cases", description = "Returns a paginated list of fraud investigation cases. "
                        + "The optional status parameter filters cases by workflow status.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud cases retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid status or pagination parameter"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required")
        })
        @GetMapping
        public ResponseEntity<Page<FraudCaseResponse>> getCases(
                        @RequestParam(required = false) FraudCaseStatus status,
                        Pageable pageable) {

                Page<FraudCaseResponse> response = fraudCaseService.getCases(status, pageable)
                                .map(FraudCaseResponse::from);

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get a fraud case", description = "Returns a specific fraud investigation case.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud case retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @GetMapping("/{caseId}")
        public ResponseEntity<FraudCaseResponse> getCase(
                        @PathVariable Long caseId) {

                return ResponseEntity.ok(
                                FraudCaseResponse.from(
                                                fraudCaseService.getCase(caseId)));
        }

        @Operation(summary = "Assign a fraud case", description = "Assigns a fraud investigation case to the specified analyst.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud case assigned successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid assignment request or workflow state"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case or analyst not found")
        })
        @PostMapping("/{caseId}/assign")
        public ResponseEntity<FraudCaseResponse> assignCase(
                        @PathVariable Long caseId,
                        @Valid @RequestBody AssignFraudCaseRequest request) {

                FraudCase fraudCase = fraudCaseService.assignCase(
                                caseId,
                                request.analystEmail());

                return ResponseEntity.ok(
                                FraudCaseResponse.from(fraudCase));
        }

        @Operation(summary = "Start fraud investigation", description = "Starts investigation of a fraud case using the authenticated analyst identity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Investigation started successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid case workflow state"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @PostMapping("/{caseId}/start")
        public ResponseEntity<FraudCaseResponse> startInvestigation(
                        @PathVariable Long caseId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                FraudCaseResponse.from(
                                                fraudCaseService.startInvestigation(
                                                                caseId,
                                                                authentication.getName())));
        }

        @Operation(summary = "Confirm fraud", description = "Marks the fraud case as confirmed fraud using the authenticated analyst identity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud confirmed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid case workflow state"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @PostMapping("/{caseId}/confirm-fraud")
        public ResponseEntity<FraudCaseResponse> confirmFraud(
                        @PathVariable Long caseId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                FraudCaseResponse.from(
                                                fraudCaseService.confirmFraud(
                                                                caseId,
                                                                authentication.getName())));
        }

        @Operation(summary = "Mark a case as false positive", description = "Marks the fraud case as a false positive using the authenticated analyst identity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Case marked as false positive"),
                        @ApiResponse(responseCode = "400", description = "Invalid case workflow state"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @PostMapping("/{caseId}/false-positive")
        public ResponseEntity<FraudCaseResponse> markFalsePositive(
                        @PathVariable Long caseId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                FraudCaseResponse.from(
                                                fraudCaseService.markFalsePositive(
                                                                caseId,
                                                                authentication.getName())));
        }

        @Operation(summary = "Close a fraud case", description = "Closes a fraud investigation case using the authenticated analyst identity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fraud case closed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid case workflow state"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @PostMapping("/{caseId}/close")
        public ResponseEntity<FraudCaseResponse> closeCase(
                        @PathVariable Long caseId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                FraudCaseResponse.from(
                                                fraudCaseService.closeCase(
                                                                caseId,
                                                                authentication.getName())));
        }

        @Operation(summary = "List investigation notes", description = "Returns a paginated list of investigation notes associated with a fraud case.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Investigation notes retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @GetMapping("/{caseId}/notes")
        public ResponseEntity<Page<InvestigationNoteResponse>> getNotes(
                        @PathVariable Long caseId,
                        Pageable pageable) {

                Page<InvestigationNoteResponse> response = investigationNoteService
                                .getNotes(caseId, pageable)
                                .map(InvestigationNoteResponse::from);

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Add an investigation note", description = "Adds an investigation note to a fraud case using the authenticated analyst identity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Investigation note created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid note request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Fraud analyst role required"),
                        @ApiResponse(responseCode = "404", description = "Fraud case not found")
        })
        @PostMapping("/{caseId}/notes")
        public ResponseEntity<InvestigationNoteResponse> addNote(
                        @PathVariable Long caseId,
                        @Valid @RequestBody CreateInvestigationNoteRequest request,
                        Authentication authentication) {

                InvestigationNote note = investigationNoteService.addNote(
                                caseId,
                                authentication.getName(),
                                request.content());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(InvestigationNoteResponse.from(note));
        }
}