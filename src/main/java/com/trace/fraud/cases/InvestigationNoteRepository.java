package com.trace.fraud.cases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationNoteRepository
        extends JpaRepository<InvestigationNote, Long> {

    Page<InvestigationNote> findByFraudCaseId(
            Long fraudCaseId,
            Pageable pageable
    );

    Page<InvestigationNote> findByAuthorId(
            Long authorId,
            Pageable pageable
    );
}