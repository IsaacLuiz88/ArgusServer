package com.argus.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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
    	// 1️⃣ encerra sessão imediatamente
    	sessionService.endByStudent(student);

    	// 1️⃣ Envia comando ao plugin
        CommandSocketEndpoint.sendShutdown(student);
        return "OK";
    }
}
