package com.argus.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/command")
public class CommandController {

    @Autowired
    private SimpMessagingTemplate ws;

    @PostMapping("/shutdown/{student}")
    public String sendShutdown(@PathVariable String student) {
        ws.convertAndSend("/topic/command/" + student, "{ \"cmd\": \"shutdown\" }");
        return "OK";
    }
}
