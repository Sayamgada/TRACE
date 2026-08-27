package com.trace.fraud.cases;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudCaseRepository
        extends JpaRepository<FraudCase, Long> {

    Optional<FraudCase> findByFraudAlertId(Long fraudAlertId);

    Page<FraudCase> findByStatus(
            FraudCaseStatus status,
            Pageable pageable
    );

    Page<FraudCase> findByAssignedAnalystId(
            Long analystId,
            Pageable pageable
    );
}