package com.infotact.heapvortex_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulerConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        // shared pool for all live-telemetry watches across sessions
        return Executors.newScheduledThreadPool(8);
    }
}