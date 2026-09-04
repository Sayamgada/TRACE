package com.trace.account.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(

        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
        BigDecimal amount
) {
}