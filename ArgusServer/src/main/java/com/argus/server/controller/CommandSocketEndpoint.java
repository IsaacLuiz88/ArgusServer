package com.argus.server.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import com.argus.server.model.Session;
import jakarta.websocket.Session;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws-command/{student}")
public class CommandSocketEndpoint {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("student") String student) {
    	System.out.println("[WS-COMMAND] conectado: " + student);
    	sessions.put(student, session);
    }

    public static void sendShutdown(String student) {
        Session s = sessions.get(student);
        if (s != null && s.isOpen()) {
            s.getAsyncRemote().sendText("{\"cmd\":\"shutdown\"}");
            System.out.println("[WS-COMMAND] shutdown enviado para " + student);
        } else {
            System.out.println("[WS-COMMAND] aluno não conectado: " + student);
        }
    }
}
