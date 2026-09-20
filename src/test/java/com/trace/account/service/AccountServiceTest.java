package com.trace.account.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.account.entity.Currency;
import com.trace.account.repository.AccountRepository;
import com.trace.audit.entity.AuditAction;
import com.trace.audit.service.AuditLogService;
import com.trace.user.entity.Role;
import com.trace.user.entity.RoleName;
import com.trace.user.entity.User;
import com.trace.user.entity.UserStatus;
import com.trace.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        Role role = new Role(RoleName.ROLE_CUSTOMER);

        user = new User(
                "Test Customer",
                "customer@test.com",
                "hashed-password",
                UserStatus.ACTIVE
        );

        user.addRole(role);

        setEntityId(user, 1L);

        account = new Account(
                "123456789012",
                user,
                new BigDecimal("10000.00"),
                Currency.INR,
                AccountStatus.ACTIVE
        );

        setEntityId(account, 10L);
    }

    @Test
    void shouldFreezeActiveAccountAndRecordAudit() {
        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response = accountService.freezeAccount(
                10L,
                authentication
        );

        assertThat(response.status())
                .isEqualTo(AccountStatus.FROZEN);

        assertThat(account.getStatus())
                .isEqualTo(AccountStatus.FROZEN);

        verify(accountRepository)
                .save(account);

        verify(auditLogService).record(
                eq(user.getId()),
                eq(AuditAction.ACCOUNT_FROZEN),
                eq("Account"),
                eq(10L),
                eq(AccountStatus.ACTIVE.name()),
                eq(AccountStatus.FROZEN.name()),
                eq(null)
        );
    }

    @Test
    void shouldRejectAlreadyFrozenAccount() {
        account.setStatus(AccountStatus.FROZEN);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                accountService.freezeAccount(
                        10L,
                        authentication
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Account is already frozen");

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(auditLogService, never()).record(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldRejectMissingAccount() {
        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                accountService.freezeAccount(
                        10L,
                        authentication
                )
        )
                .isInstanceOf(
                        com.trace.common.exception.ResourceNotFoundException.class
                )
                .hasMessage("Account not found: 10");

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(auditLogService, never()).record(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    private void setEntityId(
            Object entity,
            Long id
    ) {
        try {
            var idField = entity.getClass()
                    .getDeclaredField("id");

            idField.setAccessible(true);
            idField.set(entity, id);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to set test entity ID",
                    e
            );
        }
    }
}
