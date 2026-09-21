package com.trace.account.controller;

import java.math.BigDecimal;
import java.util.List;

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

import com.trace.account.dto.AccountResponse;
import com.trace.account.dto.CreateAccountRequest;
import com.trace.account.dto.DepositRequest;
import com.trace.account.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Validated
@Tag(name = "Accounts", description = "Customer account creation, retrieval, balance, and deposit operations")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

        private final AccountService accountService;

        public AccountController(AccountService accountService) {
                this.accountService = accountService;
        }

        @Operation(summary = "Create an account", description = "Creates a new account for the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Account created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid account request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @PostMapping
        public ResponseEntity<AccountResponse> createAccount(
                        @Valid @RequestBody CreateAccountRequest request,
                        Authentication authentication) {

                AccountResponse response = accountService.createAccount(request, authentication);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @Operation(summary = "Get my accounts", description = "Returns all accounts owned by the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping
        public ResponseEntity<List<AccountResponse>> getMyAccounts(
                        Authentication authentication) {

                return ResponseEntity.ok(
                                accountService.getMyAccounts(authentication));
        }

        @Operation(summary = "Get my accounts using the current-user endpoint", description = "Returns all accounts owned by the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/me")
        public ResponseEntity<List<AccountResponse>> getMyAccountsForCurrentUser(
                        Authentication authentication) {

                return ResponseEntity.ok(
                                accountService.getMyAccounts(authentication));
        }

        @Operation(summary = "Get an account", description = "Returns an account owned by the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required or account ownership denied"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/{accountId}")
        public ResponseEntity<AccountResponse> getMyAccount(
                        @PathVariable Long accountId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                accountService.getMyAccount(
                                                accountId,
                                                authentication));
        }

        @Operation(summary = "Get account balance", description = "Returns the balance of an account owned by the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required or account ownership denied"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/{accountId}/balance")
        public ResponseEntity<BigDecimal> getBalance(
                        @PathVariable Long accountId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                accountService.getMyAccount(
                                                accountId,
                                                authentication)
                                                .balance());
        }

        @Operation(summary = "Deposit funds", description = "Deposits funds into an account owned by the authenticated customer.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Deposit completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid deposit request"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Customer role required or account ownership denied"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        @PreAuthorize("hasRole('CUSTOMER')")
        @PostMapping("/{accountId}/deposit")
        public ResponseEntity<AccountResponse> deposit(
                        @PathVariable Long accountId,
                        @Valid @RequestBody DepositRequest request,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                accountService.deposit(
                                                accountId,
                                                request,
                                                authentication));
        }
}