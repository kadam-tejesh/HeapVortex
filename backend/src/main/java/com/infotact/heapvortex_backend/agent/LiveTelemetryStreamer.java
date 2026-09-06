package com.infotact.heapvortex_backend.agent;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class LiveTelemetryStreamer {

    private final TelemetryCollector telemetryCollector;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> activeWatches = new ConcurrentHashMap<>();

    public LiveTelemetryStreamer(TelemetryCollector telemetryCollector,
                                 java.util.concurrent.ScheduledExecutorService scheduler) {
        this.telemetryCollector = telemetryCollector;
        this.scheduler = scheduler;
    }

    /**
     * Starts polling a target JVM's telemetry every second and pushes
     * each reading to the given callback (wired to a WebSocket send).
     * Keyed by sessionId so each client's watch can be stopped independently.
     */
    public void startWatch(String sessionId, String pid, Consumer<Map<String, Object>> onUpdate) {
        stopWatch(sessionId); // replace any existing watch for this session

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> telemetry = telemetryCollector.collectTelemetry(pid);
                onUpdate.accept(telemetry);
            } catch (Exception e) {
                onUpdate.accept(Map.of("error", e.getMessage(), "pid", pid));
                stopWatch(sessionId); // stop polling a JVM that's gone/unreachable
            }
        }, 0, 1, TimeUnit.SECONDS);

        activeWatches.put(sessionId, future);
    }

    public void stopWatch(String sessionId) {
        ScheduledFuture<?> existing = activeWatches.remove(sessionId);
        if (existing != null) {
            existing.cancel(true);
        }
    }
}