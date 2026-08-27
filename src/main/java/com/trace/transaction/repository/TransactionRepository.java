package com.trace.transaction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    Page<Transaction> findBySenderAccountId(
            Long accountId,
            Pageable pageable
    );

    Page<Transaction> findByReceiverAccountId(
            Long accountId,
            Pageable pageable
    );

    List<Transaction> findByStatus(TransactionStatus status);
}