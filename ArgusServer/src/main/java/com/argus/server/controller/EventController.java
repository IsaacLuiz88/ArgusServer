package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;

import com.argus.server.model.Event;
import com.argus.server.model.EventEntity;
import com.argus.server.repository.EventRepository;
import com.argus.server.service.ActivityService;
import com.argus.server.service.EventService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final List<Event> events = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private ActivityService activityService;

    @PostMapping
    public String receiveEvent(@RequestBody Event event) throws JsonProcessingException {
    	event.setTimestamp(System.currentTimeMillis());
    	events.add(event);

    	// persist
        EventEntity ent = new EventEntity();
        ent.setType(event.getType());
        ent.setAction(event.getAction());
        ent.setCode(event.getCode());
        ent.setX(event.getX());
        ent.setY(event.getY());
        ent.setTime(event.getTime());
        ent.setStudent(event.getStudent());
        ent.setExam(event.getExam());
        ent.setTimestamp(event.getTimestamp());
        ent.setImage(event.getImage());
        eventService.save(ent);
        ent.setRaw(new ObjectMapper().writeValueAsString(event));

        eventRepository.save(ent); // salvar somente uma vez
        activityService.touch(event.getStudent(), event.getExam()); // atualizar Redis
        saveEventToLog(event, event.getStudent(), event.getExam()); // salvar em arquivo
        messagingTemplate.convertAndSend("/topic/events", event); // enviar para WebSocket /dashboard
        return "OK";
    }

    @GetMapping
    public List<Event> listEvents() {
        return events;
    }

    private void saveEventToLog(Event event, String student, String exam) {
        try {
        	String dir = "logs/" + (exam != null ? exam : "default");
            Files.createDirectories(Paths.get(dir));

            String fileName = dir + "/" + (student != null ? student : "Anonimo") + ".log";
            Files.write(
                Paths.get(fileName),
                (event.toString() + System.lineSeparator()).getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}