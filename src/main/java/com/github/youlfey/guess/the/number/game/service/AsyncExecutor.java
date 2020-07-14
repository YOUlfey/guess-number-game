package com.github.youlfey.guess.the.number.game.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AsyncExecutor {
    private static final String THREAD_POOL_TASK_EXECUTOR_FOR_ASYNC_EXECUTION = "threadPoolTaskExecutorForAsyncMode";

    private final ThreadPoolTaskExecutor executor;

    public AsyncExecutor() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(String.format("%s--", THREAD_POOL_TASK_EXECUTOR_FOR_ASYNC_EXECUTION));
        executor.initialize();
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }
}
