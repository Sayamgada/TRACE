package com.trace.fraud.controller;

import java.math.BigDecimal;
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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.trace.common.exception.GlobalExceptionHandler;
import com.trace.common.exception.ResourceNotFoundException;
import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.fraud.service.FraudAlertService;
import com.trace.transaction.entity.Transaction;

class FraudAlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FraudAlertService fraudAlertService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = standaloneSetup(
                new FraudAlertController(fraudAlertService)
        )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldReturnPaginatedAlerts() throws Exception {

        FraudAlert alert = createAlert(
                1L,
                101L,
                new BigDecimal("75.00"),
                FraudAlertSeverity.HIGH,
                FraudAlertStatus.OPEN,
                "High amount"
        );

        Page<FraudAlert> page = new PageImpl<>(
                List.of(alert),
                PageRequest.of(0, 10),
                1
        );

        when(fraudAlertService.getAlerts(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/fraud/alerts")
                        .param("page", "0")
                        .param("size", "10")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].transactionId").value(101))
                .andExpect(jsonPath("$.content[0].riskScore").value(75.00))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.content[0].reasons").value("High amount"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldFilterAlertsByStatus() throws Exception {

        FraudAlert alert = createAlert(
                1L,
                100L,
                new BigDecimal("45.00"),
                FraudAlertSeverity.MEDIUM,
                FraudAlertStatus.OPEN,
                "High amount"
        );

        Page<FraudAlert> page = new PageImpl<>(
                List.of(alert),
                PageRequest.of(0, 20),
                1
        );

        when(fraudAlertService.getAlertsByStatus(
                eq(FraudAlertStatus.OPEN),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/fraud/alerts")
                        .param("status", "OPEN")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].transactionId").value(100))
                .andExpect(jsonPath("$.content[0].severity").value("MEDIUM"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.content[0].reasons").value("High amount"));

        verify(fraudAlertService).getAlertsByStatus(
                eq(FraudAlertStatus.OPEN),
                any(Pageable.class)
        );
    }

    @Test
    void shouldFilterAlertsBySeverity() throws Exception {

        FraudAlert alert = createAlert(
                2L,
                102L,
                new BigDecimal("85.00"),
                FraudAlertSeverity.HIGH,
                FraudAlertStatus.INVESTIGATING,
                "Unusual location"
        );

        Page<FraudAlert> page = new PageImpl<>(
                List.of(alert),
                PageRequest.of(0, 20),
                1
        );

        when(fraudAlertService.getAlertsBySeverity(
                eq(FraudAlertSeverity.HIGH),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/fraud/alerts")
                        .param("severity", "HIGH")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].transactionId").value(102))
                .andExpect(jsonPath("$.content[0].riskScore").value(85.00))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.content[0].status").value("INVESTIGATING"))
                .andExpect(jsonPath("$.content[0].reasons").value("Unusual location"));

        verify(fraudAlertService).getAlertsBySeverity(
                eq(FraudAlertSeverity.HIGH),
                any(Pageable.class)
        );
    }

    @Test
    void shouldFilterAlertsByStatusAndSeverity() throws Exception {

        FraudAlert alert = createAlert(
                3L,
                103L,
                new BigDecimal("95.00"),
                FraudAlertSeverity.CRITICAL,
                FraudAlertStatus.OPEN,
                "Multiple risk rules"
        );

        Page<FraudAlert> page = new PageImpl<>(
                List.of(alert),
                PageRequest.of(0, 20),
                1
        );

        when(fraudAlertService.getAlertsByStatusAndSeverity(
                eq(FraudAlertStatus.OPEN),
                eq(FraudAlertSeverity.CRITICAL),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/fraud/alerts")
                        .param("status", "OPEN")
                        .param("severity", "CRITICAL")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].transactionId").value(103))
                .andExpect(jsonPath("$.content[0].riskScore").value(95.00))
                .andExpect(jsonPath("$.content[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.content[0].reasons").value("Multiple risk rules"));

        verify(fraudAlertService).getAlertsByStatusAndSeverity(
                eq(FraudAlertStatus.OPEN),
                eq(FraudAlertSeverity.CRITICAL),
                any(Pageable.class)
        );
    }

    @Test
    void shouldReturnAlertDetails() throws Exception {

        FraudAlert alert = createAlert(
                5L,
                105L,
                new BigDecimal("90.00"),
                FraudAlertSeverity.CRITICAL,
                FraudAlertStatus.OPEN,
                "High amount; New device"
        );

        when(fraudAlertService.getAlert(5L))
                .thenReturn(alert);

        mockMvc.perform(
                get("/api/fraud/alerts/5")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.transactionId").value(105))
                .andExpect(jsonPath("$.riskScore").value(90.00))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.reasons").value("High amount; New device"));
    }

    @Test
    void shouldReturn404WhenAlertDoesNotExist() throws Exception {

        when(fraudAlertService.getAlert(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Fraud alert not found: 999"
                        )
                );

        mockMvc.perform(
                get("/api/fraud/alerts/999")
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Fraud alert not found: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/fraud/alerts/999"));
    }

    private FraudAlert createAlert(
            Long alertId,
            Long transactionId,
            BigDecimal riskScore,
            FraudAlertSeverity severity,
            FraudAlertStatus status,
            String reasons
    ) {
        Transaction transaction = org.mockito.Mockito.mock(
                Transaction.class
        );

        when(transaction.getId())
                .thenReturn(transactionId);

        FraudAlert alert = new FraudAlert(
                transaction,
                riskScore,
                severity,
                status,
                reasons
        );

        setAlertId(alert, alertId);

        return alert;
    }

    private void setAlertId(
            FraudAlert alert,
            Long id
    ) {
        try {
            var field = FraudAlert.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(alert, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unable to set alert id for test",
                    exception
            );
        }
    }
}
