package com.infotact.heapvortex_backend.heap;

import org.netbeans.lib.profiler.heap.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GcRootFinder {

    /**
     * Depth-first search from every GC root, building the object reference
     * graph as ObjectNode/ReferenceEdge pairs your WebSocket already streams.
     *
     * We cap traversal depth/node count so we don't try to serialize the
     * entire heap in one shot — the frontend brief expects ~10,000 nodes,
     * not millions.
     */
    public GraphResult findReachableGraph(Heap heap, int maxNodes) {
        Set<Long> visited = new HashSet<>();
        List<Instance> nodeOrder = new ArrayList<>();
        List<long[]> edges = new ArrayList<>(); // [sourceInstanceId, targetInstanceId]

        Collection<GCRoot> roots = heap.getGCRoots();

        for (GCRoot root : roots) {
            if (nodeOrder.size() >= maxNodes) break;
            Instance rootInstance = root.getInstance();
            if (rootInstance != null) {
                dfs(rootInstance, visited, nodeOrder, edges, maxNodes);
            }
        }

        Set<Long> rootIds = new HashSet<>();
        for (GCRoot root : roots) {
            if (root.getInstance() != null) {
                rootIds.add(root.getInstance().getInstanceId());
            }
        }

        return new GraphResult(nodeOrder, edges, rootIds);
    }

    private void dfs(Instance instance, Set<Long> visited, List<Instance> nodeOrder,
                     List<long[]> edges, int maxNodes) {
        if (nodeOrder.size() >= maxNodes) return;
        long id = instance.getInstanceId();
        if (visited.contains(id)) return;

        visited.add(id);
        nodeOrder.add(instance);

        for (Object refObj : instance.getReferences()) {
            if (nodeOrder.size() >= maxNodes) return;
            if (refObj instanceof Value) {
                Instance target = ((Value) refObj).getDefiningInstance();
                if (target != null) {
                    edges.add(new long[]{id, target.getInstanceId()});
                    dfs(target, visited, nodeOrder, edges, maxNodes);
                }
            }
        }
    }

    public record GraphResult(List<Instance> nodes, List<long[]> edges, Set<Long> rootIds) {}
}
