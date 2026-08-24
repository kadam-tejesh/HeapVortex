package com.infotact.heapvortex_backend.heap;

import com.infotact.heapvortex_backend.agent.JmxConnector;
import com.sun.tools.attach.VirtualMachine;
import sun.tools.attach.HotSpotVirtualMachine;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.File;

@Component
public class HeapDumpTrigger {

    private final JmxConnector jmxConnector;

    public HeapDumpTrigger(JmxConnector jmxConnector) {
        this.jmxConnector = jmxConnector;
    }

    public String triggerDumpOnRemote(String targetPid, String outputDir) throws Exception {
        VirtualMachine vm = jmxConnector.attach(targetPid);
        try {
            String fileName = outputDir + "/heapdump-" + targetPid + "-" + System.currentTimeMillis() + ".hprof";
            String canonicalPath = new File(fileName).getCanonicalPath();

            HotSpotVirtualMachine hsVm = (HotSpotVirtualMachine) vm;
            try (InputStream in = hsVm.dumpHeap((Object) canonicalPath, (Object) "-live")) {
                // dumpHeap streams a small status/progress response; drain it
                byte[] buf = new byte[8192];
                while (in.read(buf) != -1) {
                    // discard — the actual dump is written directly to canonicalPath
                }
            }

            return canonicalPath;
        } finally {
            vm.detach();
        }
    }
}
