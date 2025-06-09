package com.historical.voting.user.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {
    @Value("${app.threads.transcoding-pool-size}")
    private int poolsize;

    @Bean("transcodeExecutor")
    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(poolsize);
    }
}
