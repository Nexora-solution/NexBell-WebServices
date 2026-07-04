package com.nexora.nexora_web_service.intercom.infrastructure.config;

import com.nexora.nexora_web_service.intercom.infrastructure.gateway.MediaHub;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MediaHub mediaHub;

    public WebSocketConfig(MediaHub mediaHub) {
        this.mediaHub = mediaHub;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Expose the WebSocket endpoint at /ws/media and allow all origins
        registry.addHandler(mediaHub, "/ws/media")
                .setAllowedOrigins("*")
                // Adding an interceptor is optional, but can be useful for debugging or session tracking
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
