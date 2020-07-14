package com.github.youlfey.guess.the.number.game.configuration;

import com.github.youlfey.guess.the.number.game.pojo.SessionInformation;
import com.github.youlfey.guess.the.number.game.util.SecretTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collections;

@Configuration
@Slf4j
public class GameConfiguration {
    public static final String OPTIMISTIC_RETRY = "retryTemplateOptimisticLock";
    public static final String THREAD_POOL_TASK_SCHEDULER_FOR_EXPIRE_GAMES = "threadPoolTaskSchedulerForExpirationGames";
    static final String SECURE_PASSWORD_FOR_INTERNAL_API = "securePasswordForInternalApi";

    @Bean(value = OPTIMISTIC_RETRY)
    @Primary
    public RetryTemplate retryTemplateOptimisticLock() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3,
                Collections.singletonMap(OptimisticLockingFailureException.class, true));
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    @Bean(value = THREAD_POOL_TASK_SCHEDULER_FOR_EXPIRE_GAMES)
    @Primary
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler poolTaskScheduler = new ThreadPoolTaskScheduler();
        poolTaskScheduler.setPoolSize(5);
        poolTaskScheduler.setThreadNamePrefix(String.format("%s--", THREAD_POOL_TASK_SCHEDULER_FOR_EXPIRE_GAMES));
        return poolTaskScheduler;
    }

    @Bean(value = SECURE_PASSWORD_FOR_INTERNAL_API)
    public String securePassword() {
        String password = SecretTokenUtils.generateNewToken();
        log.warn("securePasswordForInternalApi: {}", password);
        return password;
    }

    @Bean
    @SessionScope
    public SessionInformation sessionInformation() {
        return new SessionInformation();
    }
}
