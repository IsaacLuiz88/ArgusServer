package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;

import com.argus.server.bdmodel.ExamEntity;
import com.argus.server.bdmodel.SessionEntity;
import com.argus.server.bdmodel.StudentEntity;
import com.argus.server.dto.SessionDTO;
import com.argus.server.mapper.SessionMapper;
import com.argus.server.service.ExamService;
import com.argus.server.service.SessionService;
import com.argus.server.service.StudentService;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/session")
public class SessionController {

	private final StudentService studentService;
    private final ExamService examService;
    private final SessionService sessionService;
    private final SimpMessagingTemplate ws;

    public SessionController(
            StudentService studentService,
            ExamService examService,
            SessionService sessionService,
            SimpMessagingTemplate ws
        ) {
            this.studentService = studentService;
            this.examService = examService;
            this.sessionService = sessionService;
            this.ws = ws;
        }

    @PostMapping("/start")
    public SessionDTO start(@RequestBody SessionDTO req) {
        StudentEntity student = studentService.findOrCreate(req.student());
        ExamEntity exam = examService.findOrCreate(req.exam());
        SessionEntity s = sessionService.findOrCreate(student, exam);

        Map<String, Object> startedPayload = new HashMap<>();
        startedPayload.put("type", "SESSION");
        startedPayload.put("action", "STARTED");
        startedPayload.put("student", s.getStudent().getName());
        startedPayload.put("exam", s.getExam().getCode());
        startedPayload.put("session", s.getSessionUuid()); // ← chave que faltava
        startedPayload.put("studentStart", s.getStartedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        // examStart só entra se a prova já foi iniciada — sem NullPointerException
        if (s.getExam().getStartedAt() != null) {
            startedPayload.put("examStart", s.getExam().getStartedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }
        ws.convertAndSend("/topic/events", startedPayload);
//        ws.convertAndSend("/topic/events", Map.of(
//        	    "type", "SESSION",
//        	    "action", "STARTED",
//        	    "student", s.getStudent().getName(),
//        	    "exam", s.getExam().getCode(),
//        	    "session", s.getSessionUuid(),
//        	    
//        	    "studentStart", s.getStartedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
//    	        "examStart", s.getExam().getStartedAt() != null ?
//    	                s.getExam().getStartedAt()
//    	                    .atZone(ZoneId.systemDefault())
//    	                    .toInstant()
//    	                    .toEpochMilli()
//    	                : null
//        	));
        
        Map<String, Object> initialPayload = new HashMap<>();
        initialPayload.put("type", "SESSION");
        initialPayload.put("action", "INITIAL_STATE");
        initialPayload.put("student", s.getStudent().getName());
        initialPayload.put("exam", s.getExam().getCode());
        initialPayload.put("session", s.getSessionUuid());
        initialPayload.put("status", "ONLINE");
        initialPayload.put("lastAction", "-");
        initialPayload.put("lastType", "-");
        ws.convertAndSend("/topic/events", initialPayload);
//        ws.convertAndSend("/topic/events", Map.of(
//        	    "type", "SESSION",
//        	    "action", "INITIAL_STATE",
//        	    "student", s.getStudent().getName(),
//        	    "exam", s.getExam().getCode(),
//        	    "session", s.getSessionUuid(),
//        	    "status", "ONLINE",
//        	    "lastAction", "-",
//        	    "lastType", "-"
//        	));

        return SessionMapper.toDTO(s);
    }

    @PostMapping("/end/{student}")
    public void end(@PathVariable String student) {
    	System.out.println("[SESSION] encerrada para " + student);
    	sessionService.endByStudent(student);
    }

    @GetMapping("/active")
    public SessionDTO getActiveSession() {
        SessionEntity s = sessionService
            .findFirstActive()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Nenhuma sessão ativa"
            ));
        return SessionMapper.toDTO(s);
    }

    @GetMapping("/active/{student}")
    public SessionDTO getActiveByStudent(@PathVariable String student) {
        SessionEntity s = sessionService
            .findActiveByStudentName(student)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Aluno não possui sessão ativa"
            ));
        return SessionMapper.toDTO(s);
    }

    @PostMapping("/exam/start/{exam}")
    public void startExam(@PathVariable String exam) {
        ExamEntity e = examService.startExam(exam);

        long examStartMillis = e.getStartedAt()
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli();

        ws.convertAndSend("/topic/events", Map.of(
            "type", "EXAM",
            "action", "STARTED",
            "exam", exam,
            "examStart", examStartMillis
        ));
    }
}
