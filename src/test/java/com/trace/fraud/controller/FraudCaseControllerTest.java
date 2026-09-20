package com.trace.fraud.controller;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.trace.common.exception.GlobalExceptionHandler;
import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.fraud.cases.FraudCase;
import com.trace.fraud.cases.FraudCaseStatus;
import com.trace.fraud.cases.InvestigationNote;
import com.trace.fraud.service.FraudCaseService;
import com.trace.fraud.service.InvestigationNoteService;
import com.trace.transaction.entity.Transaction;

class FraudCaseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FraudCaseService fraudCaseService;

    @Mock
    private InvestigationNoteService investigationNoteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = standaloneSetup(
                new FraudCaseController(
                        fraudCaseService,
                        investigationNoteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateCaseFromAlert() throws Exception {

        FraudCase fraudCase = createCase(
                1L,
                10L,
                null,
                null,
                FraudCaseStatus.OPEN);

        when(fraudCaseService.createCase(10L))
                .thenReturn(fraudCase);

        mockMvc.perform(
                post("/api/fraud/cases/from-alert/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fraudAlertId").value(10))
                .andExpect(jsonPath("$.assignedAnalystId").doesNotExist())
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(fraudCaseService).createCase(10L);
    }

    @Test
    void shouldReturnCaseDetails() throws Exception {

        FraudCase fraudCase = createCase(
                2L,
                20L,
                100L,
                "analyst@test.com",
                FraudCaseStatus.ASSIGNED);

        when(fraudCaseService.getCase(2L))
                .thenReturn(fraudCase);

        mockMvc.perform(
                get("/api/fraud/cases/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.fraudAlertId").value(20))
                .andExpect(jsonPath("$.assignedAnalystId").value(100))
                .andExpect(jsonPath("$.assignedAnalystEmail")
                        .value("analyst@test.com"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void shouldReturnPaginatedCases() throws Exception {

        FraudCase fraudCase = createCase(
                3L,
                30L,
                null,
                null,
                FraudCaseStatus.OPEN);

        Page<FraudCase> page = new PageImpl<>(
                List.of(fraudCase),
                PageRequest.of(0, 10),
                1);

        when(fraudCaseService.getCases(
                eq(FraudCaseStatus.OPEN),
                any(Pageable.class))).thenReturn(page);

        mockMvc.perform(
                get("/api/fraud/cases")
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].fraudAlertId").value(30))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(fraudCaseService).getCases(
                eq(FraudCaseStatus.OPEN),
                any(Pageable.class));
    }

    @Test
    void shouldAssignCase() throws Exception {

        FraudCase fraudCase = createCase(
                4L,
                40L,
                200L,
                "analyst@test.com",
                FraudCaseStatus.ASSIGNED);

        when(fraudCaseService.assignCase(
                4L,
                "analyst@test.com")).thenReturn(fraudCase);

        mockMvc.perform(
                post("/api/fraud/cases/4/assign")
                        .contentType("application/json")
                        .content("""
                                {
                                    "analystEmail": "analyst@test.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.assignedAnalystId").value(200))
                .andExpect(jsonPath("$.assignedAnalystEmail")
                        .value("analyst@test.com"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        verify(fraudCaseService).assignCase(
                4L,
                "analyst@test.com");
    }

    @Test
    void shouldStartInvestigation() throws Exception {

        FraudCase fraudCase = createCase(
                5L,
                50L,
                200L,
                "analyst@test.com",
                FraudCaseStatus.INVESTIGATING);

        when(fraudCaseService.startInvestigation(5L))
                .thenReturn(fraudCase);

        mockMvc.perform(
                post("/api/fraud/cases/5/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("INVESTIGATING"));

        verify(fraudCaseService)
                .startInvestigation(5L);
    }

    @Test
    void shouldConfirmFraud() throws Exception {

        FraudCase fraudCase = createCase(
                6L,
                60L,
                200L,
                "analyst@test.com",
                FraudCaseStatus.CONFIRMED_FRAUD);

        when(fraudCaseService.confirmFraud(6L))
                .thenReturn(fraudCase);

        mockMvc.perform(
                post("/api/fraud/cases/6/confirm-fraud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED_FRAUD"))
                .andExpect(jsonPath("$.resolvedAt")
                        .exists());
    }

    @Test
    void shouldMarkFalsePositive() throws Exception {

        FraudCase fraudCase = createCase(
                7L,
                70L,
                200L,
                "analyst@test.com",
                FraudCaseStatus.FALSE_POSITIVE);

        when(fraudCaseService.markFalsePositive(7L))
                .thenReturn(fraudCase);

        mockMvc.perform(
                post("/api/fraud/cases/7/false-positive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("FALSE_POSITIVE"))
                .andExpect(jsonPath("$.resolvedAt")
                        .exists());
    }

    @Test
    void shouldCloseCase() throws Exception {

        FraudCase fraudCase = createCase(
                8L,
                80L,
                200L,
                "analyst@test.com",
                FraudCaseStatus.CLOSED);

        when(fraudCaseService.closeCase(8L))
                .thenReturn(fraudCase);

        mockMvc.perform(
                post("/api/fraud/cases/8/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CLOSED"));
    }

    @Test
    void shouldReturnInvestigationNotes() throws Exception {

        InvestigationNote note = createNote(
                1L,
                8L,
                200L,
                "analyst@test.com",
                "Transaction requires investigation.");

        Page<InvestigationNote> page = new PageImpl<>(
                List.of(note),
                PageRequest.of(0, 10),
                1);

        when(investigationNoteService.getNotes(
                eq(8L),
                any(Pageable.class))).thenReturn(page);

        mockMvc.perform(
                get("/api/fraud/cases/8/notes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].caseId").value(8))
                .andExpect(jsonPath("$.content[0].authorId").value(200))
                .andExpect(jsonPath("$.content[0].authorEmail")
                        .value("analyst@test.com"))
                .andExpect(jsonPath("$.content[0].content")
                        .value("Transaction requires investigation."));

        verify(investigationNoteService).getNotes(
                eq(8L),
                any(Pageable.class));
    }

    @Test
    void shouldAddInvestigationNote() throws Exception {

        InvestigationNote note = createNote(
                2L,
                8L,
                200L,
                "analyst@test.com",
                "Suspicious transaction requires review.");

        when(investigationNoteService.addNote(
                eq(8L),
                eq("analyst@test.com"),
                eq("Suspicious transaction requires review."))).thenReturn(note);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "analyst@test.com",
                null);

        mockMvc.perform(
                post("/api/fraud/cases/8/notes")
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "content": "Suspicious transaction requires review."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.caseId").value(8))
                .andExpect(jsonPath("$.authorId").value(200))
                .andExpect(jsonPath("$.authorEmail")
                        .value("analyst@test.com"))
                .andExpect(jsonPath("$.content")
                        .value("Suspicious transaction requires review."));

        verify(investigationNoteService).addNote(
                8L,
                "analyst@test.com",
                "Suspicious transaction requires review.");
    }

    @Test
    void shouldReturn404WhenCaseDoesNotExist() throws Exception {

        when(fraudCaseService.getCase(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Fraud case not found: 999"));

        mockMvc.perform(
                get("/api/fraud/cases/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Fraud case not found: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/fraud/cases/999"));
    }

    @Test
    void shouldRejectInvalidAssignmentRequest() throws Exception {

        mockMvc.perform(
                post("/api/fraud/cases/4/assign")
                        .contentType("application/json")
                        .content("""
                                {
                                    "analystEmail": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectBlankInvestigationNote() throws Exception {

        mockMvc.perform(
                post("/api/fraud/cases/8/notes")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "analyst@test.com",
                                        null))
                        .contentType("application/json")
                        .content("""
                                {
                                    "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"));
    }

    private FraudCase createCase(
            Long caseId,
            Long alertId,
            Long analystId,
            String analystEmail,
            FraudCaseStatus status) {
        Transaction transaction = org.mockito.Mockito.mock(
                Transaction.class);

        when(transaction.getId())
                .thenReturn(1000L + alertId);

        FraudAlert alert = new FraudAlert(
                transaction,
                java.math.BigDecimal.valueOf(85),
                FraudAlertSeverity.HIGH,
                FraudAlertStatus.OPEN,
                "Suspicious transaction");

        setId(alert, alertId);

        FraudCase fraudCase = new FraudCase(
                alert,
                status);

        setId(fraudCase, caseId);

        if (analystId != null) {
            com.trace.user.entity.User analyst = org.mockito.Mockito.mock(
                    com.trace.user.entity.User.class);

            when(analyst.getId()).thenReturn(analystId);
            when(analyst.getEmail()).thenReturn(analystEmail);

            fraudCase.assignAnalyst(analyst);

            if (status != FraudCaseStatus.ASSIGNED) {
                fraudCase.setStatus(status);
            }
        }

        if (status == FraudCaseStatus.CONFIRMED_FRAUD
                || status == FraudCaseStatus.FALSE_POSITIVE
                || status == FraudCaseStatus.CLOSED) {
            fraudCase.setResolvedAt(Instant.now());
        }

        return fraudCase;
    }

    private InvestigationNote createNote(
            Long noteId,
            Long caseId,
            Long authorId,
            String authorEmail,
            String content) {
        FraudCase fraudCase = createCase(
                caseId,
                100L + caseId,
                authorId,
                authorEmail,
                FraudCaseStatus.INVESTIGATING);

        com.trace.user.entity.User author = org.mockito.Mockito.mock(
                com.trace.user.entity.User.class);

        when(author.getId()).thenReturn(authorId);
        when(author.getEmail()).thenReturn(authorEmail);

        InvestigationNote note = new InvestigationNote(
                fraudCase,
                author,
                content);

        setId(note, noteId);

        return note;
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unable to set test id",
                    exception);
        }
    }
}