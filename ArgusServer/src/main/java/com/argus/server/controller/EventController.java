package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;

import com.argus.server.model.Event;
import com.argus.server.service.EventService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/event")
public class EventController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EventService eventService;

    @PostMapping
    public String receiveEvent(@RequestBody Event event){
    	try{
    		eventService.handleIncomingEvent(event);
	    	messagingTemplate.convertAndSend("/topic/events", event); // enviar para WebSocket /dashboard
	        return "OK";
	    	}catch(Exception e) {
	    		throw new RuntimeException("Erro ao processar evento", e);
    	}
    }
}