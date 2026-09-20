package com.trace.fraud.cases.dto;

import java.time.Instant;

import com.trace.fraud.cases.InvestigationNote;

public record InvestigationNoteResponse(
        Long id,
        Long caseId,
        Long authorId,
        String authorEmail,
        String content,
        Instant createdAt) {

    public static InvestigationNoteResponse from(InvestigationNote note) {
        return new InvestigationNoteResponse(
                note.getId(),
                note.getFraudCase().getId(),
                note.getAuthor().getId(),
                note.getAuthor().getEmail(),
                note.getContent(),
                note.getCreatedAt());
    }
}