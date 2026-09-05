package com.trace.account.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.trace.account.entity.Account;
import com.trace.account.entity.AccountStatus;
import com.trace.account.entity.Currency;
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
class AccountTransactionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldPersistUserAccountsAndTransaction() {
        TestData data = createTestData();

        Transaction transaction = transactionRepository.save(
                new Transaction(
                        "TXN-100001",
                        data.sender(),
                        data.receiver(),
                        new BigDecimal("1500.00"),
                        "INR",
                        TransactionType.TRANSFER,
                        TransactionStatus.PENDING,
                        BigDecimal.ZERO
                )
        );

        transactionRepository.flush();

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getTransactionReference())
                .isEqualTo("TXN-100001");

        assertThat(transaction.getSenderAccount().getId())
                .isEqualTo(data.sender().getId());

        assertThat(transaction.getReceiverAccount().getId())
                .isEqualTo(data.receiver().getId());

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("1500.00");

        assertThat(transaction.getStatus())
                .isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void shouldFindSenderTransactionsWithinTimeRange() {
        TestData data = createTestData();

        Transaction transaction = saveTransaction(
                "TXN-100002",
                data.sender(),
                data.receiver(),
                "2000.00"
        );

        Instant end = Instant.now().plus(1, ChronoUnit.SECONDS);
        Instant start = transaction.getCreatedAt().minus(1, ChronoUnit.SECONDS);

        List<Transaction> results =
                transactionRepository
                        .findBySenderAccountIdAndCreatedAtBetween(
                                data.sender().getId(),
                                start,
                                end
                        );

        assertThat(results)
                .hasSize(1)
                .containsExactly(transaction);
    }

    @Test
    void shouldFindHistoricalSenderTransactionsExcludingCurrentTransaction() {
        TestData data = createTestData();

        Transaction historicalTransaction = saveTransaction(
                "TXN-100003",
                data.sender(),
                data.receiver(),
                "1000.00"
        );

        Transaction currentTransaction = saveTransaction(
                "TXN-100004",
                data.sender(),
                data.receiver(),
                "60000.00"
        );

        Instant end = Instant.now().plus(1, ChronoUnit.SECONDS);
        Instant start = historicalTransaction.getCreatedAt()
                .minus(1, ChronoUnit.SECONDS);

        List<Transaction> results =
                transactionRepository
                        .findBySenderAccountIdAndIdNotAndCreatedAtBetween(
                                data.sender().getId(),
                                currentTransaction.getId(),
                                start,
                                end
                        );

        assertThat(results)
                .contains(historicalTransaction)
                .doesNotContain(currentTransaction);
    }

    @Test
    void shouldNotReturnTransactionsFromAnotherSenderAccount() {
        TestData data = createTestData();

        User secondUser = new User(
                "Second Customer",
                "second.customer@test.com",
                "hashed-password",
                UserStatus.ACTIVE
        );

        secondUser = userRepository.save(secondUser);

        Account secondSender = accountRepository.save(
                new Account(
                        "ACC-100003",
                        secondUser,
                        new BigDecimal("20000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        Transaction firstTransaction = saveTransaction(
                "TXN-100005",
                data.sender(),
                data.receiver(),
                "1000.00"
        );

        Transaction secondTransaction = saveTransaction(
                "TXN-100006",
                secondSender,
                data.receiver(),
                "2000.00"
        );

        Instant end = Instant.now().plus(1, ChronoUnit.SECONDS);
        Instant start = firstTransaction.getCreatedAt()
                .minus(1, ChronoUnit.SECONDS);

        List<Transaction> results =
                transactionRepository
                        .findBySenderAccountIdAndCreatedAtBetween(
                                data.sender().getId(),
                                start,
                                end
                        );

        assertThat(results)
                .contains(firstTransaction)
                .doesNotContain(secondTransaction);
    }

    private Transaction saveTransaction(
            String reference,
            Account sender,
            Account receiver,
            String amount
    ) {
        Transaction transaction = transactionRepository.save(
                new Transaction(
                        reference,
                        sender,
                        receiver,
                        new BigDecimal(amount),
                        "INR",
                        TransactionType.TRANSFER,
                        TransactionStatus.APPROVED,
                        BigDecimal.ZERO
                )
        );

        transactionRepository.flush();

        return transaction;
    }

    private TestData createTestData() {
        Role role = roleRepository.save(
                new Role(RoleName.ROLE_CUSTOMER)
        );

        User user = new User(
                "Test Customer",
                "customer@test.com",
                "hashed-password",
                UserStatus.ACTIVE
        );

        user.addRole(role);
        user = userRepository.save(user);

        Account sender = accountRepository.save(
                new Account(
                        "ACC-100001",
                        user,
                        new BigDecimal("10000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        Account receiver = accountRepository.save(
                new Account(
                        "ACC-100002",
                        user,
                        new BigDecimal("5000.00"),
                        Currency.INR,
                        AccountStatus.ACTIVE
                )
        );

        return new TestData(sender, receiver);
    }

    private record TestData(
            Account sender,
            Account receiver
    ) {
    }
}
