package com.banklab.fdservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.banklab.fdservice.entity.FdAccountSequence;
import com.banklab.fdservice.repository.FdAccountSequenceRepository;

/**
 * Fires many concurrent generateAccountNumber() calls for one branch and asserts
 * the pessimistic lock in FdAccountSequenceRepository serializes them into a
 * gap-free, duplicate-free run of sequence numbers.
 *
 * ddl-auto is set to "none" and the FD_ACCOUNT_SEQUENCE table is created by hand
 * (below) rather than letting Hibernate auto-generate a schema for every @Entity
 * on the classpath — FdAccount/FdAccountRole use MSSQL-only columnDefinition
 * ("UNIQUEIDENTIFIER"), which H2 can't parse, and DataJpaTest's default
 * create-drop would otherwise try to create tables for them too.
 *
 * The class-level @Transactional(NOT_SUPPORTED) overrides DataJpaTest's default
 * per-test transaction (which would pin every thread to one connection/rollback)
 * so each worker thread's @Transactional service call gets its own connection and
 * genuinely races the others.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.hikari.maximum-pool-size=50"
})
@Import(AccountNumberGeneratorService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AccountNumberGeneratorServiceConcurrencyTest {

    private static final String BRANCH_CD = "101";
    private static final String PREFIX = "FD";
    private static final int SEQ_WIDTH = 6;
    private static final int THREAD_COUNT = 50;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FdAccountSequenceRepository sequenceRepository;

    @Autowired
    private AccountNumberGeneratorService service;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS FD_ACCOUNT_SEQUENCE");
            statement.execute("""
                    CREATE TABLE FD_ACCOUNT_SEQUENCE (
                        FDSQ_BRANCH_CD VARCHAR(10) PRIMARY KEY,
                        FDSQ_BRANCH_NAME VARCHAR(100),
                        FDSQ_PREFIX VARCHAR(6),
                        FDSQ_LAST_SEQ BIGINT NOT NULL,
                        FDSQ_SEQ_WIDTH INT NOT NULL,
                        FDSQ_EFCTV_DT DATE,
                        USER_ID VARCHAR(32),
                        WS_ID VARCHAR(32),
                        LOCAL_TS TIMESTAMP,
                        HOST_TS TIMESTAMP,
                        RULE_SYSTEM_ID VARCHAR(32),
                        ACPT_TS TIMESTAMP,
                        ACPT_TS_UTC_OFST VARCHAR(6),
                        "UUID" VARCHAR(36),
                        CRUD_VALUE VARCHAR(1)
                    )
                    """);
        }

        sequenceRepository.saveAndFlush(FdAccountSequence.builder()
                .branchCd(BRANCH_CD)
                .branchName("Test Branch")
                .prefix(PREFIX)
                .lastSeq(0L)
                .seqWidth(SEQ_WIDTH)
                .build());
    }

    @Test
    void generatesUniqueGapFreeAccountNumbersUnderConcurrentLoad() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<String>> tasks = IntStream.range(0, THREAD_COUNT)
                .<Callable<String>>mapToObj(i -> () -> {
                    ready.countDown();
                    start.await();
                    return service.generateAccountNumber(BRANCH_CD);
                })
                .collect(Collectors.toList());

        try {
            List<Future<String>> futures = tasks.stream().map(pool::submit).collect(Collectors.toList());

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<String> accountNumbers = futures.stream().map(f -> {
                try {
                    return f.get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            assertThat(accountNumbers).hasSize(THREAD_COUNT);

            Set<String> distinctAccountNumbers = new HashSet<>(accountNumbers);
            assertThat(distinctAccountNumbers)
                    .as("every generated account number must be unique")
                    .hasSize(THREAD_COUNT);

            Set<Long> sequenceValues = accountNumbers.stream()
                    .map(this::extractSequence)
                    .collect(Collectors.toSet());
            Set<Long> expectedSequenceValues = sequenceRange(1, THREAD_COUNT);
            assertThat(sequenceValues)
                    .as("sequence values must be exactly 1..N with no gaps or duplicates")
                    .isEqualTo(expectedSequenceValues);

            FdAccountSequence updated = sequenceRepository.findById(BRANCH_CD).orElseThrow();
            assertThat(updated.getLastSeq()).isEqualTo(THREAD_COUNT);
        } finally {
            pool.shutdownNow();
        }
    }

    private long extractSequence(String accountNumber) {
        String suffix = accountNumber.substring(PREFIX.length() + BRANCH_CD.length());
        return Long.parseLong(suffix);
    }

    private static Set<Long> sequenceRange(long startInclusive, long count) {
        return LongStream.range(startInclusive, startInclusive + count).boxed().collect(Collectors.toSet());
    }
}
