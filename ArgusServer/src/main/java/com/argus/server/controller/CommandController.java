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

    @Autowired
    public CommandController(
            SimpMessagingTemplate ws,
            SessionService sessionService
    ) {
        this.ws = ws;
        this.sessionService = sessionService;
    }

    @PostMapping("/shutdown/{student}")
    public String sendShutdown(@PathVariable String student) {

    	 // 1️⃣ captura sessão ativa ANTES de encerrar
        var activeSession = sessionService.findActiveByStudentName(student);

        // 2️⃣ encerra sessão
        sessionService.endByStudent(student);

        // 3️⃣ envia comando ao plugin
        CommandSocketEndpoint.sendShutdown(student);

        // 3️⃣ evento oficial para dashboard + student
        ws.convertAndSend(
            "/topic/events",
            Map.of(
	    	     "type", "SESSION",
	             "action", "ENDED",
	             "student", student,
	             "exam", activeSession
	                 .map(s -> s.getExam().getCode())
	                 .orElse("DESCONHECIDO")
	         )
        );
        return "OK";
    }
}
