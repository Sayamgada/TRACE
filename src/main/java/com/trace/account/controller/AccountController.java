package com.trace.account.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.trace.account.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication
    ) {
        AccountResponse response =
                accountService.createAccount(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                accountService.getMyAccounts(authentication)
        );
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getMyAccount(
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                accountService.getMyAccount(
                        accountId,
                        authentication
                )
        );
    }
}
