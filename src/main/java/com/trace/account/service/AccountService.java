package com.trace.account.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.account.dto.AccountResponse;
import com.trace.account.dto.CreateAccountRequest;
import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

import com.trace.account.repository.AccountRepository;
import com.trace.common.exception.ResourceNotFoundException;

@Service
public class AccountService {

    private static final int ACCOUNT_NUMBER_LENGTH = 12;

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountResponse createAccount(
            CreateAccountRequest request,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account(
                accountNumber,
                user,
                BigDecimal.ZERO.setScale(2),
                request.currency(),
                AccountStatus.ACTIVE
        );

        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(
            Long accountId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId
                ));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Account not found: " + accountId
            );
        }

        return AccountResponse.from(account);
    }

    private User getAuthenticatedUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user was not found"
                ));
    }

    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private String generateAccountNumber() {

        StringBuilder builder =
                new StringBuilder(ACCOUNT_NUMBER_LENGTH);

        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            builder.append(secureRandom.nextInt(10));
        }

        return builder.toString();
    }
}
