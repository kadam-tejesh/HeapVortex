package com.infotact.heapvortex_backend.heap;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class HeapDumpParser {

    /**
     * Parses a binary .hprof file into a Heap object we can walk.
     */
    public Heap parse(String hprofFilePath) throws Exception {
        File dumpFile = new File(hprofFilePath);
        return HeapFactory.createHeap(dumpFile);
    }

    /**
     * Convenience: list every loaded class and how many live instances it has.
     * Useful for spotting an obviously bloated class (e.g. "50,000 HashMap$Node").
     */
    public List<String> summarizeByClass(Heap heap) {
        List<String> summary = new ArrayList<>();
        for (JavaClass javaClass : heap.getAllClasses()) {
            int count = javaClass.getInstancesCount();
            if (count > 0) {
                summary.add(javaClass.getName() + " -> " + count + " instances");
            }
        }
        return summary;
    }
}
