package com.trace.transaction.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RestController;

import com.trace.transaction.dto.CreateTransferRequest;
import com.trace.transaction.dto.TransactionResponse;
import com.trace.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {

        private final TransactionService transactionService;

        public TransactionController(TransactionService transactionService) {
                this.transactionService = transactionService;
        }


        @PreAuthorize("hasRole('CUSTOMER')")
        @PostMapping
        public ResponseEntity<TransactionResponse> createTransfer(
                        @Valid @RequestBody CreateTransferRequest request,
                        Authentication authentication) {

                TransactionResponse response = transactionService.createTransfer(
                                request,
                                authentication);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping
        public ResponseEntity<Page<TransactionResponse>> getMyTransactions(
                        Authentication authentication,
                        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

                return ResponseEntity.ok(
                                transactionService.getMyTransactions(
                                                authentication,
                                                pageable));
        }

        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/history")
        public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
                        Authentication authentication,
                        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

                return ResponseEntity.ok(
                                transactionService.getMyTransactions(
                                                authentication,
                                                pageable));
        }

        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/{transactionId}")
        public ResponseEntity<TransactionResponse> getMyTransaction(
                        @PathVariable Long transactionId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                transactionService.getMyTransaction(
                                                transactionId,
                                                authentication));
        }
}
