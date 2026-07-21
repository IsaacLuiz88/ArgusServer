package com.argus.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.argus.server.controller.CommandSocketHandler;

// Canal WebSocket puro (sem STOMP) usado pelo plugin Argus. Registrado à parte
// de WebSocketConfig (STOMP/SockJS) de propósito: os dois mecanismos coexistem
// bem quando cada um cuida do seu próprio path e nenhum deles depende do
// ServerEndpointExporter (JSR-356), que é a combinação que causava os
// problemas de heartbeat.
@Configuration
@EnableWebSocket
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final CommandSocketHandler commandSocketHandler;

    public RawWebSocketConfig(CommandSocketHandler commandSocketHandler) {
        this.commandSocketHandler = commandSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "*" casa com o segmento da sessão (/ws-command/<uuid>); o UUID em si
        // é extraído manualmente da URI dentro do CommandSocketHandler.
        registry.addHandler(commandSocketHandler, "/ws-command/*")
                .setAllowedOriginPatterns("*");
    }
}
