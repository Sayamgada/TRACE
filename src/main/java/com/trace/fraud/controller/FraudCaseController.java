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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fraud/cases")
@Validated
@PreAuthorize("hasRole('FRAUD_ANALYST')")
public class FraudCaseController {
        private final FraudCaseService fraudCaseService;
        private final InvestigationNoteService investigationNoteService;

        public FraudCaseController(FraudCaseService fraudCaseService,
                        InvestigationNoteService investigationNoteService) {
                this.fraudCaseService = fraudCaseService;
                this.investigationNoteService = investigationNoteService;
        }

        @PostMapping("/from-alert/{alertId}")
        public ResponseEntity<FraudCaseResponse> createCase(@PathVariable Long alertId) {
                FraudCase fraudCase = fraudCaseService.createCase(alertId);
                return ResponseEntity.status(HttpStatus.CREATED).body(FraudCaseResponse.from(fraudCase));
        }

        @GetMapping
        public ResponseEntity<Page<FraudCaseResponse>> getCases(@RequestParam(required = false) FraudCaseStatus status,
                        Pageable pageable) {
                Page<FraudCaseResponse> response = fraudCaseService.getCases(status, pageable)
                                .map(FraudCaseResponse::from);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{caseId}")
        public ResponseEntity<FraudCaseResponse> getCase(@PathVariable Long caseId) {
                return ResponseEntity.ok(FraudCaseResponse.from(fraudCaseService.getCase(caseId)));
        }

        @PostMapping("/{caseId}/assign")
        public ResponseEntity<FraudCaseResponse> assignCase(@PathVariable Long caseId,
                        @Valid @RequestBody AssignFraudCaseRequest request) {
                FraudCase fraudCase = fraudCaseService.assignCase(caseId, request.analystEmail());
                return ResponseEntity.ok(FraudCaseResponse.from(fraudCase));
        }

        @PostMapping("/{caseId}/start")
        public ResponseEntity<FraudCaseResponse> startInvestigation(@PathVariable Long caseId,
                        Authentication authentication) {
                return ResponseEntity.ok(FraudCaseResponse
                                .from(fraudCaseService.startInvestigation(caseId, authentication.getName())));
        }

        @PostMapping("/{caseId}/confirm-fraud")
        public ResponseEntity<FraudCaseResponse> confirmFraud(@PathVariable Long caseId,
                        Authentication authentication) {
                return ResponseEntity.ok(FraudCaseResponse
                                .from(fraudCaseService.confirmFraud(caseId, authentication.getName())));
        }

        @PostMapping("/{caseId}/false-positive")
        public ResponseEntity<FraudCaseResponse> markFalsePositive(@PathVariable Long caseId,
                        Authentication authentication) {
                return ResponseEntity.ok(FraudCaseResponse
                                .from(fraudCaseService.markFalsePositive(caseId, authentication.getName())));
        }

        @PostMapping("/{caseId}/close")
        public ResponseEntity<FraudCaseResponse> closeCase(@PathVariable Long caseId, Authentication authentication) {
                return ResponseEntity.ok(
                                FraudCaseResponse.from(fraudCaseService.closeCase(caseId, authentication.getName())));
        }

        @GetMapping("/{caseId}/notes")
        public ResponseEntity<Page<InvestigationNoteResponse>> getNotes(@PathVariable Long caseId, Pageable pageable) {
                Page<InvestigationNoteResponse> response = investigationNoteService.getNotes(caseId, pageable)
                                .map(InvestigationNoteResponse::from);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{caseId}/notes")
        public ResponseEntity<InvestigationNoteResponse> addNote(@PathVariable Long caseId,
                        @Valid @RequestBody CreateInvestigationNoteRequest request, Authentication authentication) {
                InvestigationNote note = investigationNoteService.addNote(caseId, authentication.getName(),
                                request.content());
                return ResponseEntity.status(HttpStatus.CREATED).body(InvestigationNoteResponse.from(note));
        }
}