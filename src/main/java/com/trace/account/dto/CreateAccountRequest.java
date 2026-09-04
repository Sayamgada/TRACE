package com.trace.account.dto;

import com.trace.account.entity.Currency;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "Currency is required")
        Currency currency

) {
}
