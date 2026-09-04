package com.trace.transaction.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransferRequest(

        @NotNull(message = "Sender account ID is required")
        @Positive(message = "Sender account ID must be positive")
        Long senderAccountId,

        @NotNull(message = "Receiver account ID is required")
        @Positive(message = "Receiver account ID must be positive")
        Long receiverAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount

) {
}