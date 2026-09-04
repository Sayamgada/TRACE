package com.trace.account.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.account.entity.Currency;

public record AccountResponse(

        Long id,
        String accountNumber,
        BigDecimal balance,
        Currency currency,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt

) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
