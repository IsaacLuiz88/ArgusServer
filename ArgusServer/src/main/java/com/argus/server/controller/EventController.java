package com.argus.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.argus.server.config.EventClassifier;
import com.argus.server.model.Event;
import com.argus.server.model.EventEntity;
import com.argus.server.repository.EventRepository;
import com.argus.server.service.ActivityService;
import com.argus.server.service.EventService;
import com.argus.server.util.CSVUtil;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.ListCrudRepository;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final List<Event> events = Collections.synchronizedList(new ArrayList<>());
    //private static final String LOG_FILE = "events_server.log";

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
    	event.setLevel(EventClassifier.classify(event));
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
        ent.setLevel(event.getLevel());
        ent.setTimestamp(event.getTimestamp());
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
//    @GetMapping("/csv")
//    public ResponseEntity<String> exportCsv() {
//        ListCrudRepository<EventEntity, Long> repo = null;
//        List<EventEntity> list = repo.findAll();
//        return ResponseEntity.ok(CSVUtil.toCSV(list));
//    }
}
    
    /*
    @PostMapping("/api/events")
    public ResponseEntity<String> receiveEvent(@RequestBody String eventJson) {
        System.out.println("Evento recebido: " + eventJson);
        // TODO: salvar no banco de dados, ou mandar pro painel do professor
        return ResponseEntity.ok("Recebido com sucesso");
    }*/
