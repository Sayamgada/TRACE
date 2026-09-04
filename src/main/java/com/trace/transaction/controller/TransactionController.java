package com.trace.transaction.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
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

    public TransactionController(
            TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            Authentication authentication
    ) {
        TransactionResponse response =
                transactionService.createTransfer(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
