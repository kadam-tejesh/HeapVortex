package com.infotact.heapvortex_backend.agent;

import com.sun.tools.attach.VirtualMachine;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@Component
public class TelemetryCollector {

    /**
     * Connects to the target JVM's local management agent and reads
     * current heap usage + thread count.
     */
    public Map<String, Object> collectTelemetry(String pid) throws Exception {
        VirtualMachine vm = VirtualMachine.attach(pid);
        try {
            String connectorAddress = vm.startLocalManagementAgent();

            JMXServiceURL url = new JMXServiceURL(connectorAddress);
            try (JMXConnector connector = JMXConnectorFactory.connect(url)) {
                MBeanServerConnection mbsc = connector.getMBeanServerConnection();

                MemoryMXBean memoryBean = ManagementFactory.newPlatformMXBeanProxy(
                        mbsc, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
                ThreadMXBean threadBean = ManagementFactory.newPlatformMXBeanProxy(
                        mbsc, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);

                Map<String, Object> telemetry = new HashMap<>();
                telemetry.put("pid", pid);
                telemetry.put("heapUsedBytes", memoryBean.getHeapMemoryUsage().getUsed());
                telemetry.put("heapMaxBytes", memoryBean.getHeapMemoryUsage().getMax());
                telemetry.put("threadCount", threadBean.getThreadCount());

                return telemetry;
            }
        } finally {
            vm.detach();
        }
    }
}
