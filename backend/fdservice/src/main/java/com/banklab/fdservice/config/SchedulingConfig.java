package com.banklab.fdservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on @Scheduled processing only when the EOD scheduler is enabled, so tests
 * and ad-hoc runs with fd.batch.scheduler.enabled=false never fire the cron.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "fd.batch.scheduler.enabled", havingValue = "true")
public class SchedulingConfig {
}
