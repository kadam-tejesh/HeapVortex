package com.infotact.heapvortex_backend.controller;

import com.infotact.heapvortex_backend.agent.JmxConnector;
import com.infotact.heapvortex_backend.agent.TelemetryCollector;
import com.infotact.heapvortex_backend.dto.JvmProcessDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jvms")
public class JvmController {

    private final JmxConnector jmxConnector;
    private final TelemetryCollector telemetryCollector;

    public JvmController(JmxConnector jmxConnector, TelemetryCollector telemetryCollector) {
        this.jmxConnector = jmxConnector;
        this.telemetryCollector = telemetryCollector;
    }

    @GetMapping
    public List<JvmProcessDto> listJvms() {
        return jmxConnector.listLocalJvms();
    }

    @GetMapping("/{pid}/telemetry")
    public Map<String, Object> getTelemetry(@PathVariable String pid) throws Exception {
        return telemetryCollector.collectTelemetry(pid);
    }
}