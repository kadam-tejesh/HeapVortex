
        package com.infotact.heapvortex_backend.controller;

import com.infotact.heapvortex_backend.agent.JmxConnector;
import com.infotact.heapvortex_backend.agent.TelemetryCollector;
import com.infotact.heapvortex_backend.dto.JvmProcessDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jvm")
public class JvmController {

    private final JmxConnector jmxConnector;
    private final TelemetryCollector telemetryCollector;

    public JvmController(
            JmxConnector jmxConnector,
            TelemetryCollector telemetryCollector) {

        this.jmxConnector = jmxConnector;
        this.telemetryCollector = telemetryCollector;
    }

    /**
     * Get all locally running JVM processes.
     *
     * GET /api/jvm/processes
     */
    @GetMapping("/processes")
    public List<JvmProcessDto> listJvms() {
        return jmxConnector.listLocalJvms();
    }

    /**
     * Connect to a JVM process.
     *
     * POST /api/jvm/connect/{pid}
     */
    @PostMapping("/connect/{pid}")
    public ResponseEntity<?> connectToJvm(
            @PathVariable String pid) throws Exception {

        // This line depends on the actual method available
        // in your JmxConnector class.
        jmxConnector.attach(pid);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Successfully connected to JVM",
                        "pid", pid
                )
        );
    }

    /**
     * Get telemetry for a JVM process.
     *
     * GET /api/jvm/{pid}/telemetry
     */
    @GetMapping("/{pid}/telemetry")
    public Map<String, Object> getTelemetry(
            @PathVariable String pid) throws Exception {

        return telemetryCollector.collectTelemetry(pid);
    }
}



