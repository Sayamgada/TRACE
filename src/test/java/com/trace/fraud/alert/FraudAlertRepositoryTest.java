package com.trace.fraud.alert;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.account.entity.Currency;
import com.trace.account.repository.AccountRepository;
import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;
import com.trace.transaction.entity.TransactionType;
import com.trace.transaction.repository.TransactionRepository;
import com.trace.user.entity.User;
import com.trace.user.entity.UserStatus;
import com.trace.user.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
class FraudAlertRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Test
    void shouldPersistAndFindFraudAlertByTransaction() {
        User user = userRepository.save(
                new User(
                        "Fraud Test Customer",
                        "fraud-test@test.com",
                        "hashed-password",
                        UserStatus.ACTIVE
                )
        );

        Account sender = accountRepository.save(
                new Account(
                        "ACC-200001",
                        user,
                        new BigDecimal("100000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        Account receiver = accountRepository.save(
                new Account(
                        "ACC-200002",
                        user,
                        new BigDecimal("10000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        Transaction transaction = transactionRepository.save(
                new Transaction(
                        "TXN-200001",
                        sender,
                        receiver,
                        new BigDecimal("90000.00"),
                        "INR",
                        TransactionType.TRANSFER,
                        TransactionStatus.BLOCKED,
                        new BigDecimal("85.00")
                )
        );

        transactionRepository.flush();

        FraudAlert alert = fraudAlertRepository.save(
                new FraudAlert(
                        transaction,
                        new BigDecimal("85.00"),
                        FraudAlertSeverity.HIGH,
                        FraudAlertStatus.OPEN,
                        "Unusually high transaction amount; New device detected"
                )
        );

        fraudAlertRepository.flush();

        assertThat(alert.getId()).isNotNull();
        assertThat(alert.getTransaction().getId())
                .isEqualTo(transaction.getId());
        assertThat(alert.getRiskScore())
                .isEqualByComparingTo("85.00");
        assertThat(alert.getSeverity())
                .isEqualTo(FraudAlertSeverity.HIGH);
        assertThat(alert.getStatus())
                .isEqualTo(FraudAlertStatus.OPEN);
        assertThat(alert.getReasons())
                .contains("Unusually high transaction amount");

        FraudAlert retrieved =
                fraudAlertRepository
                        .findByTransactionId(transaction.getId())
                        .orElseThrow();

        assertThat(retrieved.getId())
                .isEqualTo(alert.getId());
        assertThat(retrieved.getTransaction().getId())
                .isEqualTo(transaction.getId());
    }
}   