package com.trace.transaction.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.account.repository.AccountRepository;
import com.trace.common.exception.ResourceNotFoundException;
import com.trace.transaction.dto.CreateTransferRequest;
import com.trace.transaction.dto.TransactionResponse;
import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;
import com.trace.transaction.entity.TransactionType;
import com.trace.transaction.repository.TransactionRepository;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse createTransfer(
            CreateTransferRequest request,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        Account senderAccount;
        Account receiverAccount;

        if (request.senderAccountId() < request.receiverAccountId()) {

            senderAccount = accountRepository.findWithLockById(request.senderAccountId())
                    .orElseThrow(()
                            -> new ResourceNotFoundException(
                            "Sender account not found: " + request.senderAccountId()));

            receiverAccount = accountRepository.findWithLockById(request.receiverAccountId())
                    .orElseThrow(()
                            -> new ResourceNotFoundException(
                            "Receiver account not found: " + request.receiverAccountId()));

        } else {

            receiverAccount = accountRepository.findWithLockById(request.receiverAccountId())
                    .orElseThrow(()
                            -> new ResourceNotFoundException(
                            "Receiver account not found: " + request.receiverAccountId()));

            senderAccount = accountRepository.findWithLockById(request.senderAccountId())
                    .orElseThrow(()
                            -> new ResourceNotFoundException(
                            "Sender account not found: " + request.senderAccountId()));
        }
        validateSenderOwnership(senderAccount, user);

        validateTransfer(
                senderAccount,
                receiverAccount,
                request.amount()
        );

        Transaction transaction = new Transaction(
                generateTransactionReference(),
                senderAccount,
                receiverAccount,
                request.amount().setScale(2),
                senderAccount.getCurrency().name(),
                TransactionType.TRANSFER,
                TransactionStatus.PENDING,
                BigDecimal.ZERO.setScale(2)
        );

        transactionRepository.save(transaction);

        transaction.setStatus(TransactionStatus.PROCESSING);

        processTransfer(
                senderAccount,
                receiverAccount,
                request.amount()
        );

        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setProcessedAt(Instant.now());

        return TransactionResponse.from(
                transactionRepository.save(transaction)
        );
    }

    private void validateSenderOwnership(
            Account senderAccount,
            User user
    ) {
        if (!senderAccount.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Sender account not found: "
                    + senderAccount.getId()
            );
        }
    }

    private void validateTransfer(
            Account senderAccount,
            Account receiverAccount,
            BigDecimal amount
    ) {
        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different"
            );
        }

        if (senderAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Sender account is not active"
            );
        }

        if (receiverAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Receiver account is not active"
            );
        }

        if (!senderAccount.getCurrency()
                .equals(receiverAccount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Sender and receiver currencies must match"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero"
            );
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Transfer amount cannot have more than 2 decimal places"
            );
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }
    }

    private void processTransfer(
            Account senderAccount,
            Account receiverAccount,
            BigDecimal amount
    ) {
        senderAccount.setBalance(
                senderAccount.getBalance().subtract(amount)
        );

        receiverAccount.setBalance(
                receiverAccount.getBalance().add(amount)
        );

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "Authentication is required"
            );
        }

        return userRepository.findByEmailIgnoreCase(
                authentication.getName()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Authenticated user was not found"
        ));
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase();
    }
}
