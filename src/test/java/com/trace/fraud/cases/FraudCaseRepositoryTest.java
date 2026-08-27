package com.trace.fraud.cases;

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
import com.trace.fraud.alert.FraudAlert;
import com.trace.fraud.alert.FraudAlertRepository;
import com.trace.fraud.alert.FraudAlertSeverity;
import com.trace.fraud.alert.FraudAlertStatus;
import com.trace.transaction.entity.Transaction;
import com.trace.transaction.entity.TransactionStatus;
import com.trace.transaction.entity.TransactionType;
import com.trace.transaction.repository.TransactionRepository;
import com.trace.user.entity.Role;
import com.trace.user.entity.RoleName;
import com.trace.user.entity.User;
import com.trace.user.entity.UserStatus;
import com.trace.user.repository.RoleRepository;
import com.trace.user.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
class FraudCaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private FraudCaseRepository fraudCaseRepository;

    @Autowired
    private InvestigationNoteRepository investigationNoteRepository;

    @Test
    void shouldPersistFraudCaseAndInvestigationNote() {

        Role analystRole = roleRepository.save(
                new Role(RoleName.ROLE_FRAUD_ANALYST)
        );

        User customer = userRepository.save(
                new User(
                        "Test Customer",
                        "customer-fraud@test.com",
                        "hashed-password",
                        UserStatus.ACTIVE
                )
        );

        User analyst = new User(
                "Test Analyst",
                "analyst@test.com",
                "hashed-password",
                UserStatus.ACTIVE
        );

        analyst.addRole(analystRole);
        analyst = userRepository.save(analyst);

        Account sender = accountRepository.save(
                new Account(
                        "ACC-300001",
                        customer,
                        new BigDecimal("100000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        Account receiver = accountRepository.save(
                new Account(
                        "ACC-300002",
                        customer,
                        new BigDecimal("10000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        Transaction transaction = transactionRepository.save(
                new Transaction(
                        "TXN-300001",
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

        FraudCase fraudCase = fraudCaseRepository.save(
                new FraudCase(
                        alert,
                        FraudCaseStatus.OPEN
                )
        );

        fraudCase.assignAnalyst(analyst);

        fraudCaseRepository.flush();

        InvestigationNote note = investigationNoteRepository.save(
                new InvestigationNote(
                        fraudCase,
                        analyst,
                        "Transaction requires investigation due to unusually high amount."
                )
        );

        investigationNoteRepository.flush();

        assertThat(fraudCase.getId()).isNotNull();

        assertThat(fraudCase.getFraudAlert().getId())
                .isEqualTo(alert.getId());

        assertThat(fraudCase.getAssignedAnalyst().getId())
                .isEqualTo(analyst.getId());

        assertThat(fraudCase.getStatus())
                .isEqualTo(FraudCaseStatus.ASSIGNED);

        assertThat(note.getId()).isNotNull();

        assertThat(note.getFraudCase().getId())
                .isEqualTo(fraudCase.getId());

        assertThat(note.getAuthor().getId())
                .isEqualTo(analyst.getId());

        assertThat(note.getContent())
                .contains("unusually high amount");

        FraudCase retrievedCase =
                fraudCaseRepository
                        .findByFraudAlertId(alert.getId())
                        .orElseThrow();

        assertThat(retrievedCase.getId())
                .isEqualTo(fraudCase.getId());

        assertThat(retrievedCase.getAssignedAnalyst().getId())
                .isEqualTo(analyst.getId());

        assertThat(retrievedCase.getStatus())
                .isEqualTo(FraudCaseStatus.ASSIGNED);
    }
}