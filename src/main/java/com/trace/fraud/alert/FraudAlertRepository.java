package com.trace.fraud.alert;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudAlertRepository
        extends JpaRepository<FraudAlert, Long> {

    Optional<FraudAlert> findByTransactionId(Long transactionId);

    Page<FraudAlert> findByStatus(
            FraudAlertStatus status,
            Pageable pageable
    );

    Page<FraudAlert> findBySeverity(
            FraudAlertSeverity severity,
            Pageable pageable
    );
}