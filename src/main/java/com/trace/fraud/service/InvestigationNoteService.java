package com.trace.fraud.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.cases.FraudCase;
import com.trace.fraud.cases.FraudCaseRepository;
import com.trace.fraud.cases.FraudCaseStatus;
import com.trace.fraud.cases.InvestigationNote;
import com.trace.fraud.cases.InvestigationNoteRepository;
import com.trace.user.entity.User;
import com.trace.user.repository.UserRepository;

@Service
public class InvestigationNoteService {

        private final InvestigationNoteRepository noteRepository;
        private final FraudCaseRepository fraudCaseRepository;
        private final UserRepository userRepository;

        public InvestigationNoteService(
                        InvestigationNoteRepository noteRepository,
                        FraudCaseRepository fraudCaseRepository,
                        UserRepository userRepository) {

                this.noteRepository = noteRepository;
                this.fraudCaseRepository = fraudCaseRepository;
                this.userRepository = userRepository;
        }

        @Transactional
        public InvestigationNote addNote(
                        Long caseId,
                        String authorEmail,
                        String content) {

                FraudCase fraudCase = fraudCaseRepository.findById(caseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Fraud case not found: " + caseId));

                User author = userRepository
                                .findByEmailIgnoreCase(authorEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found: " + authorEmail));

                if (fraudCase.getStatus() == FraudCaseStatus.CLOSED) {
                        throw new IllegalStateException(
                                        "Cannot add a note to a closed case");
                }

                if (fraudCase.getAssignedAnalyst() == null
                                || !fraudCase.getAssignedAnalyst()
                                                .getId()
                                                .equals(author.getId())) {

                        throw new IllegalStateException(
                                        "Investigation note can only be added by the assigned analyst");
                }

                InvestigationNote note = new InvestigationNote(
                                fraudCase,
                                author,
                                content.trim());

                return noteRepository.save(note);
        }

        @Transactional(readOnly = true)
        public Page<InvestigationNote> getNotes(
                        Long caseId,
                        Pageable pageable) {

                if (!fraudCaseRepository.existsById(caseId)) {
                        throw new ResourceNotFoundException(
                                        "Fraud case not found: " + caseId);
                }

                return noteRepository.findByFraudCaseId(
                                caseId,
                                pageable);
        }
}