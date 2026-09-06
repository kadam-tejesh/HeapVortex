package com.infotact.heapvortex_backend.websocket;

import com.infotact.heapvortex_backend.agent.LiveTelemetryStreamer;
import com.infotact.heapvortex_backend.heap.HeapAnalysisService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class GraphStreamHandler extends TextWebSocketHandler {

    private final HeapAnalysisService analysisService;
    private final LiveTelemetryStreamer telemetryStreamer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public GraphStreamHandler(HeapAnalysisService analysisService,
                              LiveTelemetryStreamer telemetryStreamer) {
        this.analysisService = analysisService;
        this.telemetryStreamer = telemetryStreamer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        sendStatus(session, "connected",
                "Send {\"action\":\"analyze\",\"pid\":\"<pid>\"} or {\"action\":\"watch\",\"pid\":\"<pid>\"}");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        executor.submit(() -> handleClientMessage(session, message.getPayload()));
    }

    private void handleClientMessage(WebSocketSession session, String payload) {
        try {
            Map<?, ?> request = mapper.readValue(payload, Map.class);
            String action = String.valueOf(request.get("action"));

            switch (action) {
                case "analyze" -> {
                    String pid = String.valueOf(request.get("pid"));
                    sendStatus(session, "analyzing", "Attaching to PID " + pid + " and dumping heap...");
                    Map<String, Object> result = analysisService.analyzeRemote(
                            pid, System.getProperty("java.io.tmpdir"), 5000);
                    sendJson(session, Map.of(
                            "type", "graph", "pid", pid,
                            "nodes", result.get("nodes"), "edges", result.get("edges")
                    ));
                }
                case "watch" -> {
                    String pid = String.valueOf(request.get("pid"));
                    sendStatus(session, "watching", "Streaming live telemetry for PID " + pid);
                    telemetryStreamer.startWatch(session.getId(), pid, telemetry ->
                            sendJson(session, Map.of("type", "telemetry", "pid", pid, "data", telemetry)));
                }
                case "unwatch" -> {
                    telemetryStreamer.stopWatch(session.getId());
                    sendStatus(session, "stopped", "Live telemetry stopped.");
                }
                default -> sendStatus(session, "error", "Unknown action: " + action);
            }
        } catch (Exception e) {
            sendStatus(session, "error", "Request failed: " + e.getMessage());
        }
    }

    private void sendStatus(WebSocketSession session, String status, String message) {
        sendJson(session, Map.of("type", "status", "status", status, "message", message));
    }

    private void sendJson(WebSocketSession session, Map<String, Object> payload) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO: proper logger
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        telemetryStreamer.stopWatch(session.getId()); // critical: stop polling when client disconnects
    }
}
