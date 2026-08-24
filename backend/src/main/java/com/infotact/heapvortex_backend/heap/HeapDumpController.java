package com.infotact.heapvortex_backend.controller;

import com.infotact.heapvortex_backend.heap.HeapAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/heapdump")
public class HeapDumpController {

    private final HeapAnalysisService analysisService;

    public HeapDumpController(HeapAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // in HeapDumpController
    @PostMapping("/analyze/{pid}")
    public Map<String, Object> analyzeRemote(
            @PathVariable String pid,
            @RequestParam(defaultValue = "10000") int maxNodes) throws Exception {
        return analysisService.analyzeRemote(pid, System.getProperty("java.io.tmpdir"), maxNodes);
    }
}