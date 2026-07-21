package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;

import com.argus.server.model.Event;
import com.argus.server.service.EventService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/event")
public class EventController {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private EventService eventService;

	@PostMapping
	public String receiveEvent(@RequestBody Event event, HttpServletRequest request) {
		try {
			String ip = extractClientIp(request);
			event.setIp(ip);

			// Chama o service para tentar persistir (ele vai retornar false para heartbeats)
	        boolean savedOnDb = eventService.handleIncomingEvent(event, ip);
	        boolean isHeartbeat = "heartbeat".equalsIgnoreCase(event.getType());
	        
	        if (savedOnDb || isHeartbeat) {
	            messagingTemplate.convertAndSend("/topic/events", event);
	        }

			return "OK";
		} catch (Exception e) {
			throw new RuntimeException("Erro ao processar evento", e);
		}
	}

	private String extractClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (ip != null && !ip.isBlank()) return ip;

		ip = request.getHeader("X-Forwarded-For");
		if (ip != null && !ip.isBlank()) return ip.split(",")[0];
		return request.getRemoteAddr();
	}
}
