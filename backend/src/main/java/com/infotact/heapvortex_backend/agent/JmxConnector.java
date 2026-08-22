package com.infotact.heapvortex_backend.agent;

import com.infotact.heapvortex_backend.dto.JvmProcessDto;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JmxConnector {

    /**
     * Lists all locally discoverable JVM processes using the Attach API.
     */
    public List<JvmProcessDto> listLocalJvms() {
        List<JvmProcessDto> jvms = new ArrayList<>();

        for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
            String pid = vmd.id();
            String displayName = vmd.displayName();
            // displayName is often "com.example.MainClass arg1 arg2" — take first token as main class
            String mainClass = displayName.split(" ")[0];

            jvms.add(new JvmProcessDto(pid, displayName, mainClass));
        }

        return jvms;
    }

    /**
     * Attaches to a target JVM by PID. Returns the VirtualMachine handle
     * so TelemetryCollector / heap dump triggers can use it.
     */
    public VirtualMachine attach(String pid) throws Exception {
        return VirtualMachine.attach(pid);
    }
}
