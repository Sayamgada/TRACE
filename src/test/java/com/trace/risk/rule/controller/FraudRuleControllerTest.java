package com.trace.risk.rule.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.common.exception.GlobalExceptionHandler;
import com.trace.risk.rule.FraudRule;
import com.trace.risk.rule.FraudRuleService;
import com.trace.risk.rule.FraudRuleType;

@ExtendWith(MockitoExtension.class)
class FraudRuleControllerTest {

    @Mock
    private FraudRuleService fraudRuleService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UsernamePasswordAuthenticationToken adminAuthentication;
    private UsernamePasswordAuthenticationToken customerAuthentication;

    @BeforeEach
    void setUp() {
        FraudRuleController controller = new FraudRuleController(fraudRuleService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        adminAuthentication = new UsernamePasswordAuthenticationToken(
                "admin@trace.local",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        customerAuthentication = new UsernamePasswordAuthenticationToken(
                "customer@trace.local",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void shouldGetFraudRules() throws Exception {
        FraudRule rule = rule(1L);

        when(fraudRuleService.getRules())
                .thenReturn(List.of(rule));

        mockMvc.perform(get("/api/admin/fraud-rules")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .authentication(
                                adminAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateFraudRule() throws Exception {
        FraudRule rule = rule(1L);

        when(fraudRuleService.createRule(
                any(),
                any(),
                any(),
                any(),
                any(Boolean.class),
                any()))
                .thenReturn(rule);

        String request = """
                {
                  "ruleName": "HIGH_AMOUNT",
                  "ruleType": "HIGH_AMOUNT",
                  "threshold": 10000.00,
                  "weight": 30.00,
                  "active": true
                }
                """;

        mockMvc.perform(post("/api/admin/fraud-rules")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .authentication(
                                adminAuthentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateFraudRule() throws Exception {
        FraudRule rule = rule(1L);

        when(fraudRuleService.updateRule(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Boolean.class),
                any()))
                .thenReturn(rule);

        String request = """
                {
                  "ruleName": "HIGH_AMOUNT_UPDATED",
                  "ruleType": "HIGH_AMOUNT",
                  "threshold": 15000.00,
                  "weight": 35.00,
                  "active": false
                }
                """;

        mockMvc.perform(put("/api/admin/fraud-rules/1")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .authentication(
                                adminAuthentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());
    }

    @Test
    void shouldToggleFraudRule() throws Exception {
        FraudRule rule = rule(1L);

        when(fraudRuleService.toggleRule(
                any(),
                any()))
                .thenReturn(rule);

        mockMvc.perform(patch("/api/admin/fraud-rules/1/toggle")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .authentication(
                                adminAuthentication))
                .with(csrf()))
                .andExpect(status().isOk());
    }


    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        String request = """
                {
                  "ruleName": "",
                  "ruleType": null,
                  "threshold": -1,
                  "weight": -1,
                  "active": true
                }
                """;

        mockMvc.perform(post("/api/admin/fraud-rules")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .authentication(
                                adminAuthentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
    }

    private FraudRule rule(Long id) {
        FraudRule rule = new FraudRule(
                "HIGH_AMOUNT",
                FraudRuleType.HIGH_AMOUNT,
                new BigDecimal("10000.00"),
                new BigDecimal("30.00"),
                true);

        org.springframework.test.util.ReflectionTestUtils.setField(
                rule, "id", id);

        return rule;
    }
}