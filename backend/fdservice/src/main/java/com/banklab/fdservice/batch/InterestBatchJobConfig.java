package com.banklab.fdservice.batch;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import com.banklab.fdservice.repository.FdAccountRepository;
import com.banklab.fdservice.service.InterestPostingService;

/**
 * Spring Batch definitions for the two FD batch jobs (OpenAPI jobName ACCRUAL /
 * MATURITY). Each job is one chunk-oriented step:
 * <ul>
 *   <li><b>reader</b> — the ids of accounts due for this business date, snapshotted when
 *       the step starts (paging over rows the writer is changing would skip rows);</li>
 *   <li><b>writer</b> — {@link InterestPostingService#accrue} / {@link InterestPostingService#mature},
 *       which call the interest calculator and the ledger writer.</li>
 * </ul>
 * Chunk size is 1, so each account is its own chunk and its own transaction.
 *
 * <h2>Failure handling</h2>
 * Transient database failures (lock timeouts, deadlocks) are retried up to
 * {@code fd.batch.retry-limit} times. Any other exception skips that one account —
 * logged, counted in FDJB_SKIP_CNT, run marked PARTIAL — and the rest of the run goes
 * on (lab manual L14 §9: "log accounts where calculation fails but proceed with
 * others"). Past {@code fd.batch.skip-limit} skips the step fails and the run is
 * FAILED. Skipped accounts keep their guard dates, so the next run retries them;
 * accounts already done are no-ops.
 */
@Configuration
public class InterestBatchJobConfig {

    public static final String ACCRUAL_JOB = "fdAccrualJob";
    public static final String MATURITY_JOB = "fdMaturityJob";
    public static final String BUSINESS_DATE_PARAM = "businessDate";

    @Value("${fd.batch.retry-limit}")
    private long retryLimit;

    @Value("${fd.batch.skip-limit}")
    private long skipLimit;

    @Bean
    Job fdAccrualJob(JobRepository jobRepository, Step fdAccrualStep) {
        return new JobBuilder(ACCRUAL_JOB, jobRepository).start(fdAccrualStep).build();
    }

    @Bean
    Job fdMaturityJob(JobRepository jobRepository, Step fdMaturityStep) {
        return new JobBuilder(MATURITY_JOB, jobRepository).start(fdMaturityStep).build();
    }

    @Bean
    Step fdAccrualStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<UUID> fdAccrualReader, ItemWriter<UUID> fdAccrualWriter, BatchSkipRecorder skipRecorder) {
        return accountStep("fdAccrualStep", jobRepository, transactionManager, fdAccrualReader, fdAccrualWriter,
                skipRecorder);
    }

    @Bean
    Step fdMaturityStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<UUID> fdMaturityReader, ItemWriter<UUID> fdMaturityWriter, BatchSkipRecorder skipRecorder) {
        return accountStep("fdMaturityStep", jobRepository, transactionManager, fdMaturityReader, fdMaturityWriter,
                skipRecorder);
    }

    @Bean
    @StepScope
    ItemReader<UUID> fdAccrualReader(@Value("#{jobParameters['" + BUSINESS_DATE_PARAM + "']}") String businessDate,
            FdAccountRepository accountRepository) {
        return new ListItemReader<>(accountRepository.findIdsDueForAccrual(LocalDate.parse(businessDate)));
    }

    @Bean
    @StepScope
    ItemReader<UUID> fdMaturityReader(@Value("#{jobParameters['" + BUSINESS_DATE_PARAM + "']}") String businessDate,
            FdAccountRepository accountRepository) {
        return new ListItemReader<>(accountRepository.findIdsDueForMaturity(LocalDate.parse(businessDate)));
    }

    @Bean
    @StepScope
    ItemWriter<UUID> fdAccrualWriter(@Value("#{jobParameters['" + BUSINESS_DATE_PARAM + "']}") String businessDate,
            InterestPostingService interestPosting) {
        LocalDate date = LocalDate.parse(businessDate);
        return chunk -> {
            for (UUID fdaId : chunk) {
                interestPosting.accrue(fdaId, date);
            }
        };
    }

    @Bean
    @StepScope
    ItemWriter<UUID> fdMaturityWriter(@Value("#{jobParameters['" + BUSINESS_DATE_PARAM + "']}") String businessDate,
            InterestPostingService interestPosting) {
        LocalDate date = LocalDate.parse(businessDate);
        return chunk -> {
            for (UUID fdaId : chunk) {
                interestPosting.mature(fdaId, date);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Step accountStep(String name, JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<UUID> reader, ItemWriter<UUID> writer, BatchSkipRecorder skipRecorder) {
        return new StepBuilder(name, jobRepository)
                .<UUID, UUID>chunk(1)
                .transactionManager(transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(retryLimit)
                .skip(Exception.class)
                .skipLimit(skipLimit)
                .skipListener(skipRecorder)
                .listener((StepExecutionListener) skipRecorder)
                .build();
    }
}
