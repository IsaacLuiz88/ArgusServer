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

    @PostMapping("/end/{student}")
    public void end(@PathVariable String student) {
        if (active != null && student.equals(active.getStudent())) {
            System.out.println("[SESSION] encerrada para " + student);
            active = null;
        }
    }
}
