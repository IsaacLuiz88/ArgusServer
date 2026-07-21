package com.argus.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import com.argus.server.service.SessionService;

@RestController
@RequestMapping("/api/command")
public class CommandController {

	private final SimpMessagingTemplate ws;
    private final SessionService sessionService;
    private final CommandSocketHandler commandSocketHandler;

    @Autowired
    public CommandController(
            SimpMessagingTemplate ws,
            SessionService sessionService,
            CommandSocketHandler commandSocketHandler
    ) {
        this.ws = ws;
        this.sessionService = sessionService;
        this.commandSocketHandler = commandSocketHandler;
    }

    @PostMapping("/shutdown/{student}")
    public String sendShutdown(@PathVariable String student) {

    	 // 1️⃣ captura sessão ativa ANTES de encerrar
        var activeSession = sessionService.findActiveByStudentName(student);

        // 2️⃣ encerra sessão
        sessionService.endByStudent(student);

        // 3️⃣ envia comando ao plugin
        activeSession.ifPresent(s -> commandSocketHandler.sendShutdown(s.getSessionUuid()));

        // 3️⃣ evento oficial para dashboard + student
        ws.convertAndSend("/topic/events",
            Map.of(
	    	     "type", "SESSION",
	             "action", "ENDED",
	             "student", student,
	             "session", activeSession.map(s -> s.getSessionUuid()).orElse(""),
	             "exam", activeSession.map(s -> s.getExam().getCode()).orElse("DESCONHECIDO")
	         )
        );
        return "OK";
    }

    @PostMapping("/shutdown-all")
    public String shutdownAll() {

        var sessions = sessionService.findAllActive();

        // 1️⃣ envia comando WS
        commandSocketHandler.sendShutdownAll();

        // 2️⃣ encerra no banco
        sessions.forEach(s -> sessionService.end(s));

        // 3️⃣ notifica dashboard
        sessions.forEach(s -> {
            ws.convertAndSend(
                "/topic/events",
                Map.of(
                    "type", "SESSION",
                    "action", "ENDED",
                    "student", s.getStudent().getName(),
                    "exam", s.getExam().getCode(),
                    "session", s.getSessionUuid()
                )
            );
        });
        return "OK";
    }
}
