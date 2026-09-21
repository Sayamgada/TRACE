package com.trace.account.controller;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.trace.account.dto.AccountResponse;
import com.trace.account.entity.AccountStatus;
import com.trace.account.entity.Currency;
import com.trace.account.service.AccountService;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

        @Mock
        private AccountService accountService;

        private AccountController controller;

        private Authentication customerAuthentication;

        @BeforeEach
        void setUp() {
                controller = new AccountController(accountService);

                customerAuthentication = new UsernamePasswordAuthenticationToken(
                                "customer@test.com",
                                "password",
                                List.of(
                                                new SimpleGrantedAuthority(
                                                                "ROLE_CUSTOMER")));
        }

        @Test
        void shouldGetMyAccountsFromMeEndpoint() {
                AccountResponse account = account();

                when(accountService.getMyAccounts(customerAuthentication))
                                .thenReturn(List.of(account));

                ResponseEntity<List<AccountResponse>> response = controller.getMyAccountsForCurrentUser(
                                customerAuthentication);

                assertThat(response.getStatusCode().value())
                                .isEqualTo(200);

                assertThat(response.getBody())
                                .hasSize(1);

                assertThat(response.getBody().get(0).id())
                                .isEqualTo(10L);

                assertThat(response.getBody().get(0).accountNumber())
                                .isEqualTo("123456789012");

                assertThat(response.getBody().get(0).balance())
                                .isEqualByComparingTo("10000.00");

                verify(accountService)
                                .getMyAccounts(customerAuthentication);
        }

        @Test
        void shouldGetAccountBalance() {
                AccountResponse account = account();

                when(accountService.getMyAccount(
                                10L,
                                customerAuthentication))
                                .thenReturn(account);

                ResponseEntity<BigDecimal> response = controller.getBalance(
                                10L,
                                customerAuthentication);

                assertThat(response.getStatusCode().value())
                                .isEqualTo(200);

                assertThat(response.getBody())
                                .isEqualByComparingTo("10000.00");

                verify(accountService)
                                .getMyAccount(
                                                10L,
                                                customerAuthentication);
        }

    private AccountResponse account() {
        return new AccountResponse(
                10L,
                "123456789012",
                new BigDecimal("10000.00"),
                Currency.INR,
                AccountStatus.ACTIVE,
                null,
                null);
    }
}
