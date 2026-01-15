package com.argus.server.controller;

import com.argus.server.controller.SessionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/command")
public class CommandController {

	@Autowired
    private SessionController sessionController;

    @Autowired
    private SimpMessagingTemplate ws;

//    @PostMapping("/shutdown/{student}")
//    public String sendShutdown(@PathVariable String student) {
//        ws.convertAndSend("/topic/command/" + student, "{ \"cmd\": \"shutdown\" }");
//        return "OK";
//    }
    @PostMapping("/shutdown/{student}")
    public String sendShutdown(@PathVariable String student) {
    	// 1️⃣ Envia comando ao plugin
        CommandSocketEndpoint.sendShutdown(student);

        // 2️⃣ Encerra sessão lógica no servidor
        sessionController.end(student);
        return "OK";
    }

}
