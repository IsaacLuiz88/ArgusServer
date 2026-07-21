package com.argus.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefixo para tópicos de broadcast (usado pelo dashboard)
        config.enableSimpleBroker("/topic");
        // Prefixo para endpoints que recebem mensagens dos clientes (se for usado)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint STOMP/SockJS acessível pelo navegador (dashboard.html / student.html)
        // O plugin (Java puro) NÃO usa este endpoint — ele se conecta direto em
        // /ws-command/{session}, um WebSocket puro registrado em RawWebSocketConfig.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
