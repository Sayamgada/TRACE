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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@Validated
@Tag(name = "Transactions", description = "Customer transaction creation and transaction history operations")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

        private final TransactionService transactionService;

        public TransactionController(TransactionService transactionService) {
                this.transactionService = transactionService;
        }

        @Operation(summary = "Create a transfer", description = "Creates a transfer for the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Transfer created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid transfer request or transaction rejected"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
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

        @Operation(summary = "Get my transactions", description = "Returns a paginated list of transactions belonging to the authenticated customer. "
                        + "Results default to 20 records sorted by creation time.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
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

        @Operation(summary = "Get transaction history", description = "Returns a paginated transaction history for the authenticated customer. "
                        + "Results default to 20 records sorted by creation time.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
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

        @Operation(summary = "Get a transaction", description = "Returns a transaction accessible to the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required or transaction ownership denied"),
                        @ApiResponse(responseCode = "404", description = "Transaction not found")
        })
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