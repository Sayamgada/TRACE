package com.trace.transaction.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
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
import com.trace.risk.evaluator.FraudEvaluationContext;
import com.trace.risk.scoring.RiskDecision;
import com.trace.risk.scoring.RiskEvaluationResult;
import com.trace.risk.scoring.RiskEvaluationService;
import com.trace.risk.scoring.RiskLevel;
import com.trace.transaction.dto.CreateTransferRequest;
import com.trace.transaction.dto.TransactionResponse;
import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;
import com.trace.transaction.repository.TransactionRepository;
import com.trace.user.entity.Role;
import com.trace.user.entity.RoleName;
import com.trace.user.entity.User;
import com.trace.user.entity.UserStatus;
import com.trace.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RiskEvaluationService riskEvaluationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account sender;
    private Account receiver;

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

        sender = new Account(
                "ACC-100001",
                user,
                new BigDecimal("10000.00"),
                Currency.INR,
                AccountStatus.ACTIVE
        );

        receiver = new Account(
                "ACC-100002",
                user,
                new BigDecimal("5000.00"),
                Currency.INR,
                AccountStatus.ACTIVE
        );

        setEntityId(sender, 1L);
        setEntityId(receiver, 2L);
    }

    @Test
    void shouldApproveTransferWhenRiskDecisionIsApprove() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                2L,
                new BigDecimal("1500.00")
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findWithLockById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findWithLockById(2L))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(riskEvaluationService.evaluate(
                any(Transaction.class),
                any(FraudEvaluationContext.class)
        )).thenReturn(
                new RiskEvaluationResult(
                        new BigDecimal("0.00"),
                        RiskLevel.LOW,
                        RiskDecision.APPROVE,
                        List.of()
                )
        );

        TransactionResponse response
                = transactionService.createTransfer(
                        request,
                        authentication
                );

        assertThat(response.status())
                .isEqualTo(TransactionStatus.APPROVED);

        assertThat(response.riskScore())
                .isEqualByComparingTo("0.00");

        assertThat(sender.getBalance())
                .isEqualByComparingTo("8500.00");

        assertThat(receiver.getBalance())
                .isEqualByComparingTo("6500.00");

        verify(accountRepository).save(sender);
        verify(accountRepository).save(receiver);
    }

    @Test
    void shouldNotMutateBalancesWhenRiskDecisionIsReview() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                2L,
                new BigDecimal("1500.00")
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findWithLockById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findWithLockById(2L))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(riskEvaluationService.evaluate(
                any(Transaction.class),
                any(FraudEvaluationContext.class)
        )).thenReturn(
                new RiskEvaluationResult(
                        new BigDecimal("30.00"),
                        RiskLevel.MEDIUM,
                        RiskDecision.REVIEW,
                        List.of("HIGH_AMOUNT")
                )
        );

        TransactionResponse response
                = transactionService.createTransfer(
                        request,
                        authentication
                );

        assertThat(response.status())
                .isEqualTo(TransactionStatus.FLAGGED);

        assertThat(response.riskScore())
                .isEqualByComparingTo("30.00");

        assertThat(sender.getBalance())
                .isEqualByComparingTo("10000.00");

        assertThat(receiver.getBalance())
                .isEqualByComparingTo("5000.00");

        verify(accountRepository, never()).save(sender);
        verify(accountRepository, never()).save(receiver);
    }

    @Test
    void shouldNotMutateBalancesWhenRiskDecisionIsBlock() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                2L,
                new BigDecimal("1500.00")
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findWithLockById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findWithLockById(2L))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(riskEvaluationService.evaluate(
                any(Transaction.class),
                any(FraudEvaluationContext.class)
        )).thenReturn(
                new RiskEvaluationResult(
                        new BigDecimal("70.00"),
                        RiskLevel.HIGH,
                        RiskDecision.BLOCK,
                        List.of("HIGH_AMOUNT", "TRANSACTION_FREQUENCY")
                )
        );

        TransactionResponse response
                = transactionService.createTransfer(
                        request,
                        authentication
                );

        assertThat(response.status())
                .isEqualTo(TransactionStatus.BLOCKED);

        assertThat(response.riskScore())
                .isEqualByComparingTo("70.00");

        assertThat(sender.getBalance())
                .isEqualByComparingTo("10000.00");

        assertThat(receiver.getBalance())
                .isEqualByComparingTo("5000.00");

        verify(accountRepository, never()).save(sender);
        verify(accountRepository, never()).save(receiver);
    }

    @Test
    void shouldPreserveInsufficientBalanceValidation() {
        sender.setBalance(new BigDecimal("500.00"));

        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                2L,
                new BigDecimal("1500.00")
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(accountRepository.findWithLockById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findWithLockById(2L))
                .thenReturn(Optional.of(receiver));

        assertThatThrownBy(()
                -> transactionService.createTransfer(
                        request,
                        authentication
                )
        )
                .isInstanceOf(InsufficientBalanceException.class);

        verify(riskEvaluationService, never())
                .evaluate(any(), any());

        assertThat(sender.getBalance())
                .isEqualByComparingTo("500.00");

        assertThat(receiver.getBalance())
                .isEqualByComparingTo("5000.00");
    }

    private void setEntityId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
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
