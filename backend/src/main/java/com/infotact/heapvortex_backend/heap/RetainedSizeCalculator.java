package com.infotact.heapvortex_backend.heap;

import org.netbeans.lib.profiler.heap.Instance;
import org.springframework.stereotype.Component;

@Component
public class RetainedSizeCalculator {

    /**
     * hprof-heap computes retained size natively per instance —
     * we just expose it cleanly for our DTO layer.
     */
    public long retainedSizeOf(Instance instance) {
        return instance.getRetainedSize();
    }

    public long shallowSizeOf(Instance instance) {
        return instance.getSize();
    }
}
