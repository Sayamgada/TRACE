package com.trace.fraud.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertRepository;
import com.trace.fraud.cases.FraudCase;
import com.trace.fraud.cases.FraudCaseRepository;
import com.trace.fraud.cases.FraudCaseStatus;
import com.trace.user.entity.RoleName;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@Service
public class FraudCaseService {

    private final FraudCaseRepository fraudCaseRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final UserRepository userRepository;

    public FraudCaseService(
            FraudCaseRepository fraudCaseRepository,
            FraudAlertRepository fraudAlertRepository,
            UserRepository userRepository) {

        this.fraudCaseRepository = fraudCaseRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FraudCase createCase(Long alertId) {

        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fraud alert not found: " + alertId));

        if (fraudCaseRepository.findByFraudAlertId(alertId).isPresent()) {
            throw new IllegalStateException(
                    "A fraud case already exists for alert: " + alertId);
        }

        FraudCase fraudCase = new FraudCase(
                alert,
                FraudCaseStatus.OPEN);

        return fraudCaseRepository.save(fraudCase);
    }

    @Transactional
    public FraudCase assignCase(
            Long caseId,
            String analystEmail) {

        FraudCase fraudCase = getCase(caseId);

        User analyst = userRepository
                .findByEmailIgnoreCase(analystEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + analystEmail));

        boolean fraudAnalyst = analyst.getRoles()
                .stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_FRAUD_ANALYST);

        if (!fraudAnalyst) {
            throw new IllegalArgumentException(
                    "User is not a fraud analyst: " + analystEmail);
        }

        fraudCase.assignAnalyst(analyst);

        return fraudCaseRepository.save(fraudCase);
    }

    @Transactional
    public FraudCase startInvestigation(Long caseId) {

        FraudCase fraudCase = getCase(caseId);

        if (fraudCase.getAssignedAnalyst() == null) {
            throw new IllegalStateException(
                    "Case must be assigned before investigation can start");
        }

        if (fraudCase.getStatus() != FraudCaseStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Case must be ASSIGNED before investigation can start");
        }

        fraudCase.setStatus(FraudCaseStatus.INVESTIGATING);

        return fraudCaseRepository.save(fraudCase);
    }

    @Transactional
    public FraudCase confirmFraud(Long caseId) {

        FraudCase fraudCase = getCase(caseId);

        validateInvestigating(fraudCase);

        fraudCase.setStatus(FraudCaseStatus.CONFIRMED_FRAUD);
        fraudCase.setResolvedAt(Instant.now());

        return fraudCaseRepository.save(fraudCase);
    }

    @Transactional
    public FraudCase markFalsePositive(Long caseId) {

        FraudCase fraudCase = getCase(caseId);

        validateInvestigating(fraudCase);

        fraudCase.setStatus(FraudCaseStatus.FALSE_POSITIVE);
        fraudCase.setResolvedAt(Instant.now());

        return fraudCaseRepository.save(fraudCase);
    }

    @Transactional
    public FraudCase closeCase(Long caseId) {

        FraudCase fraudCase = getCase(caseId);

        if (fraudCase.getStatus() != FraudCaseStatus.CONFIRMED_FRAUD
                && fraudCase.getStatus() != FraudCaseStatus.FALSE_POSITIVE) {

            throw new IllegalStateException(
                    "Case must be resolved before it can be closed");
        }

        fraudCase.setStatus(FraudCaseStatus.CLOSED);

        return fraudCaseRepository.save(fraudCase);
    }

    @Transactional(readOnly = true)
    public FraudCase getCase(Long caseId) {
        return fraudCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fraud case not found: " + caseId));
    }

    @Transactional(readOnly = true)
    public Page<FraudCase> getCases(
            FraudCaseStatus status,
            Pageable pageable) {

        if (status != null) {
            return fraudCaseRepository.findByStatus(
                    status,
                    pageable);
        }

        return fraudCaseRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<FraudCase> getCasesAssignedToAnalyst(
            String analystEmail,
            Pageable pageable) {

        User analyst = userRepository
                .findByEmailIgnoreCase(analystEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + analystEmail));

        return fraudCaseRepository.findByAssignedAnalystId(
                analyst.getId(),
                pageable);
    }

    private void validateInvestigating(FraudCase fraudCase) {

        if (fraudCase.getStatus() != FraudCaseStatus.INVESTIGATING) {

            throw new IllegalStateException(
                    "Case must be under investigation");
        }
    }
}