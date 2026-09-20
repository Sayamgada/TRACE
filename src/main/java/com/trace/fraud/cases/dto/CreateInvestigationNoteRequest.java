package com.trace.fraud.cases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInvestigationNoteRequest(
        @NotBlank @Size(max = 5000) String content) {
}