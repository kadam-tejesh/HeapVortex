package com.infotact.heapvortex_backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GraphStreamHandler graphStreamHandler;

    public WebSocketConfig(GraphStreamHandler graphStreamHandler) {
        this.graphStreamHandler = graphStreamHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler((WebSocketHandler) graphStreamHandler, "/ws/graph")
                .setAllowedOrigins("*"); // tighten this before Week 4 (TLS/security hardening)
    }
}