package com.argus.server.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.argus.server.model.Event;
import com.argus.server.service.ActivityService;
import com.argus.server.service.SessionService;

// Canal do plugin (Java puro / cliente desktop), separado do STOMP usado pelo
// dashboard (/ws). Implementado com o WebSocketHandler nativo do Spring em vez
// de um @ServerEndpoint JSR-356: assim é um bean Spring normal (singleton,
// @Autowired funciona de verdade) e não depende do ServerEndpointExporter, que
// convive mal com @EnableWebSocketMessageBroker no mesmo Tomcat embutido.
@Component
public class CommandSocketHandler extends TextWebSocketHandler {

    private static final String SESSION_ATTR = "argusSessionUuid";

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final SessionService sessionService;
    private final ActivityService activityService;
    private final SimpMessagingTemplate messagingTemplate;

    public CommandSocketHandler(
            SessionService sessionService,
            ActivityService activityService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.sessionService = sessionService;
        this.activityService = activityService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) {
        String sessionUuid = extractSessionUuid(wsSession);
        wsSession.getAttributes().put(SESSION_ATTR, sessionUuid);
        sessions.put(sessionUuid, wsSession);
        System.out.println("[WS-COMMAND] conectado: " + sessionUuid);

        // Plugin que estava offline quando a prova foi encerrada (ou que reconectou depois):
        // manda encerrar na hora, em vez de deixá-lo monitorando uma sessão já finalizada.
        sessionService.findByUuid(sessionUuid)
                .filter(s -> !"ACTIVE".equals(s.getStatus()))
                .ifPresent(s -> {
                    System.out.println("[WS-COMMAND] sessão já encerrada, enviando shutdown: " + sessionUuid);
                    sendShutdown(sessionUuid);
                });
    }

    @Override
    protected void handleTextMessage(WebSocketSession wsSession, TextMessage message) {
        String sessionUuid = (String) wsSession.getAttributes().get(SESSION_ATTR);
        String msg = message.getPayload();

        if (msg.contains("\"type\": \"heartbeat\"") || msg.contains("\"type\":\"heartbeat\"")) {
            handleHeartbeat(sessionUuid);
            return;
        }

        if (msg.contains("\"cmd\":\"SHUTDOWN_OK\"")) {
            System.out.println("[WS-COMMAND] encerramento confirmado: " + sessionUuid);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) {
        String sessionUuid = (String) wsSession.getAttributes().get(SESSION_ATTR);
        // remove(chave, valor): só desmapeia se ainda for ESTE socket. Com reconexão, o socket
        // antigo pode fechar depois de o novo conectar, e não pode apagar o novo.
        sessions.remove(sessionUuid, wsSession);
        System.out.println("[WS-COMMAND] desconectado: " + sessionUuid + " (" + status + ")");
    }

    private String extractSessionUuid(WebSocketSession wsSession) {
        String path = wsSession.getUri().getPath(); // /ws-command/{session}
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    // Mesmo efeito de um heartbeat via HTTP (/api/event): atualiza a atividade
    // da sessão e replica o evento para o dashboard via /topic/events.
    private void handleHeartbeat(String sessionUuid) {
        System.out.println("[WS-COMMAND] heartbeat recebido: " + sessionUuid);

        sessionService.findByUuid(sessionUuid).ifPresentOrElse(session -> {
            if (!"ACTIVE".equals(session.getStatus())) {
                sendShutdown(sessionUuid); // heartbeat de sessão encerrada: não conta como atividade
                return;
            }
            Event event = new Event();
            event.setType("heartbeat");
            event.setSession(sessionUuid);
            event.setStudent(session.getStudent().getName());
            event.setExam(session.getExam().getCode());
            event.setTimestamp(System.currentTimeMillis());

            activityService.touch(session, event);
            messagingTemplate.convertAndSend("/topic/events", event);
        }, () -> System.out.println("[WS-COMMAND] heartbeat ignorado - sessão não encontrada: " + sessionUuid));
    }

    public void sendShutdown(String sessionUuid) {
        WebSocketSession s = sessions.get(sessionUuid);
        if (s != null && s.isOpen()) {
            sendText(s, "{\"cmd\":\"shutdown\"}");
            System.out.println("[WS-COMMAND] shutdown enviado para " + sessionUuid);
        } else {
            System.out.println("[WS-COMMAND] aluno não conectado: " + sessionUuid);
        }
    }

    public void sendShutdownAll() {
        sessions.forEach((sessionUuid, s) -> {
            if (s.isOpen()) {
                sendText(s, "{\"cmd\":\"shutdown\"}");
                System.out.println("[WS-COMMAND] shutdown enviado para " + sessionUuid);
            }
        });
    }

    private void sendText(WebSocketSession s, String text) {
        try {
            synchronized (s) {
                s.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            System.err.println("[WS-COMMAND] falha ao enviar mensagem: " + e.getMessage());
        }
    }
}
