package com.infotact.heapvortex_backend.heap;

import com.infotact.heapvortex_backend.dto.ObjectNodeDto;
import com.infotact.heapvortex_backend.dto.ReferenceEdgeDto;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HeapAnalysisService {

    private final HeapDumpTrigger trigger;
    private final HeapDumpParser parser;
    private final GcRootFinder gcRootFinder;
    private final RetainedSizeCalculator sizeCalculator;

    public HeapAnalysisService(HeapDumpTrigger trigger, HeapDumpParser parser,
                               GcRootFinder gcRootFinder, RetainedSizeCalculator sizeCalculator) {
        this.trigger = trigger;
        this.parser = parser;
        this.gcRootFinder = gcRootFinder;
        this.sizeCalculator = sizeCalculator;
    }

    /**
     * Full pipeline: trigger dump -> parse -> DFS from roots -> build the
     * exact node/edge DTOs the WebSocket already streams to Elizabeth's UI.
     */
    // in HeapAnalysisService
    public Map<String, Object> analyzeRemote(String targetPid, String outputDir, int maxNodes) throws Exception {
        String dumpFile = trigger.triggerDumpOnRemote(targetPid, outputDir);
        Heap heap = parser.parse(dumpFile);
        GcRootFinder.GraphResult result = gcRootFinder.findReachableGraph(heap, maxNodes);
        // ... same node/edge building as before


        List<ObjectNodeDto> nodes = new ArrayList<>();
        for (Instance instance : result.nodes()) {
            nodes.add(new ObjectNodeDto(
                    String.valueOf(instance.getInstanceId()),
                    instance.getJavaClass().getName(),
                    sizeCalculator.retainedSizeOf(instance),
                    result.rootIds().contains(instance.getInstanceId())
            ));
        }

        List<ReferenceEdgeDto> edges = new ArrayList<>();
        for (long[] edge : result.edges()) {
            edges.add(new ReferenceEdgeDto(
                    String.valueOf(edge[0]), String.valueOf(edge[1]), "field"
            ));
        }

        return Map.of("nodes", nodes, "edges", edges, "dumpFile", dumpFile);
    }
}
