package com.infotact.heapvortex_backend.websocket;

import tools.jackson.databind.ObjectMapper;
import com.infotact.heapvortex_backend.dto.ObjectNodeDto;
import com.infotact.heapvortex_backend.dto.ReferenceEdgeDto;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class GraphStreamHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> activeStreams = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Every 2s, push a small dummy graph so the frontend has real traffic to render.
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(buildDummyPayload())));
                }
            } catch (Exception e) {
                // TODO: replace with proper logging (Week 2+)
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);

        activeStreams.put(session.getId(), future);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ScheduledFuture<?> future = activeStreams.remove(session.getId());
        if (future != null) {
            future.cancel(true);
        }
    }

    private Map<String, Object> buildDummyPayload() {
        List<ObjectNodeDto> nodes = List.of(
                new ObjectNodeDto("n1", "java.util.HashMap", 204800, true),
                new ObjectNodeDto("n2", "com.infotact.heapvortex_backend.SomeService", 51200, false),
                new ObjectNodeDto("n3", "java.lang.String", 1024, false)
        );

        List<ReferenceEdgeDto> edges = List.of(
                new ReferenceEdgeDto("n1", "n2", "field"),
                new ReferenceEdgeDto("n2", "n3", "field")
        );

        return Map.of("nodes", nodes, "edges", edges);
    }
}
