package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;
import com.argus.server.model.Session;
import com.argus.server.service.SessionService;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionService sessionService;
    
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/end/{student}")
    public void end(@PathVariable String student) {
    	System.out.println("[SESSION] encerrada para " + student);
    	sessionService.endByStudent(student);
    }
}
