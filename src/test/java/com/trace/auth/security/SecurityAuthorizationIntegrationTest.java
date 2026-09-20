package com.trace.auth.security;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.account.entity.Currency;
import com.trace.account.repository.AccountRepository;
import com.trace.user.entity.Role;
import com.trace.user.entity.RoleName;
import com.trace.user.entity.User;
import com.trace.user.entity.UserStatus;
import com.trace.user.repository.RoleRepository;
import com.trace.user.repository.UserRepository;

import jakarta.servlet.Filter;

@SpringBootTest
@ActiveProfiles("test")
class SecurityAuthorizationIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private Filter springSecurityFilterChain;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private AccountRepository accountRepository;

        private MockMvc mockMvc;

        private User customerOne;
        private User customerTwo;

        private Account accountOne;
        private Account accountTwo;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(context)
                                .apply(springSecurity(springSecurityFilterChain))
                                .build();

                accountRepository.deleteAll();
                userRepository.deleteAll();

                Role customerRole = roleRepository
                                .findByName(RoleName.ROLE_CUSTOMER)
                                .orElseGet(() -> roleRepository.save(
                                                new Role(RoleName.ROLE_CUSTOMER)));

                customerOne = new User(
                                "Customer One",
                                "customer.one@example.com",
                                "hashed-password",
                                UserStatus.ACTIVE);

                customerOne.addRole(customerRole);

                customerTwo = new User(
                                "Customer Two",
                                "customer.two@example.com",
                                "hashed-password",
                                UserStatus.ACTIVE);

                customerTwo.addRole(customerRole);

                customerOne = userRepository.save(customerOne);
                customerTwo = userRepository.save(customerTwo);

                accountOne = accountRepository.save(
                                new Account(
                                                "100000000001",
                                                customerOne,
                                                new BigDecimal("10000.00"),
                                                Currency.INR,
                                                AccountStatus.ACTIVE));

                accountTwo = accountRepository.save(
                                new Account(
                                                "100000000002",
                                                customerTwo,
                                                new BigDecimal("10000.00"),
                                                Currency.INR,
                                                AccountStatus.ACTIVE));
        }

        @Test
        void customerCannotAccessFraudAlerts() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/alerts")
                                                .with(user("customer.one@example.com")
                                                                .roles("CUSTOMER")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void fraudAnalystCanAccessFraudAlerts() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/alerts")
                                                .with(user("analyst@example.com")
                                                                .roles("FRAUD_ANALYST")))
                                .andExpect(status().isOk());
        }

        @Test
        void unauthenticatedUserCannotAccessFraudAlerts()
                        throws Exception {

                mockMvc.perform(
                                get("/api/fraud/alerts"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void customerCanAccessOwnAccount() throws Exception {
                mockMvc.perform(
                                get("/api/accounts/{accountId}", accountOne.getId())
                                                .with(user(customerOne.getEmail())
                                                                .roles("CUSTOMER")))
                                .andExpect(status().isOk());
        }

        @Test
        void customerCannotAccessAnotherCustomersAccount()
                        throws Exception {

                mockMvc.perform(
                                get("/api/accounts/{accountId}", accountTwo.getId())
                                                .with(user(customerOne.getEmail())
                                                                .roles("CUSTOMER")))
                                .andExpect(status().isNotFound());
        }

        @Test
        void customerCannotDepositIntoAnotherCustomersAccount()
                        throws Exception {

                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                                .post("/api/accounts/{accountId}/deposit",
                                                                accountTwo.getId())
                                                .with(user(customerOne.getEmail())
                                                                .roles("CUSTOMER"))
                                                .contentType("application/json")
                                                .content("""
                                                                {
                                                                    "amount": 100.00
                                                                }
                                                                """))
                                .andExpect(status().isNotFound());
        }

        @Test
        void unauthenticatedUserCannotAccessAccount()
                        throws Exception {

                mockMvc.perform(
                                get("/api/accounts/{accountId}", accountOne.getId()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void bankEmployeeCannotAccessCustomerAccounts() throws Exception {
                mockMvc.perform(
                                get("/api/accounts/{accountId}", accountOne.getId())
                                                .with(user("employee@example.com")
                                                                .roles("BANK_EMPLOYEE")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void administratorCannotAccessCustomerAccounts() throws Exception {
                mockMvc.perform(
                                get("/api/accounts/{accountId}", accountOne.getId())
                                                .with(user("admin@example.com")
                                                                .roles("ADMIN")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void auditorCannotAccessCustomerAccounts() throws Exception {
                mockMvc.perform(
                                get("/api/accounts/{accountId}", accountOne.getId())
                                                .with(user("auditor@example.com")
                                                                .roles("AUDITOR")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void bankEmployeeCannotAccessFraudAlerts() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/alerts")
                                                .with(user("employee@example.com")
                                                                .roles("BANK_EMPLOYEE")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void administratorCannotAccessFraudAlerts() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/alerts")
                                                .with(user("admin@example.com")
                                                                .roles("ADMIN")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void auditorCannotAccessFraudAlerts() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/alerts")
                                                .with(user("auditor@example.com")
                                                                .roles("AUDITOR")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void unauthenticatedUserCannotAccessFraudCases() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/cases"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void customerCannotAccessFraudCases() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/cases")
                                                .with(user("customer@example.com").roles("CUSTOMER")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void bankEmployeeCannotAccessFraudCases() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/cases")
                                                .with(user("employee@example.com").roles("BANK_EMPLOYEE")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminCannotAccessFraudCases() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/cases")
                                                .with(user("admin@example.com").roles("ADMIN")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void auditorCannotAccessFraudCases() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/cases")
                                                .with(user("auditor@example.com").roles("AUDITOR")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void fraudAnalystCanAccessFraudCases() throws Exception {
                mockMvc.perform(
                                get("/api/fraud/cases")
                                                .with(user("analyst@example.com").roles("FRAUD_ANALYST")))
                                .andExpect(status().isOk());
        }
}