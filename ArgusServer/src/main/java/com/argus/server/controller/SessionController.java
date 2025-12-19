package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;
import com.argus.server.model.Session;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private volatile Session active;

    @PostMapping("/start")
    public void start(@RequestBody Session s) {
        this.active = s;
    }

    @GetMapping("/active")
    public Session active() {
        return active;
    }
}
