package com.argus.server.service;

import org.springframework.stereotype.Service;
import com.argus.server.model.Event;
import com.argus.server.bdmodel.*;
import com.argus.server.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class EventService {

	private final EventRepository eventRepository;
    private final StudentService studentService;
    private final ExamService examService;
    private final SessionService sessionService;
    private final ActivityService activityService;

    private final ObjectMapper mapper = new ObjectMapper();

    public EventService(
            EventRepository eventRepository,
            StudentService studentService,
            ExamService examService,
            SessionService sessionService,
            ActivityService activityService
    ) {
        this.eventRepository = eventRepository;
        this.studentService = studentService;
        this.examService = examService;
        this.sessionService = sessionService;
        this.activityService = activityService;
    }

    public void handleIncomingEvent(Event event) throws Exception {

        // 1️⃣ Timestamp do servidor (equivale ao event.setTimestamp)
        long receivedAt = System.currentTimeMillis();
        event.setTimestamp(receivedAt);

        // 2️⃣ Resolve ou cria Student
        StudentEntity student = studentService.findOrCreate(event.getStudent());

        // 3️⃣ Resolve ou cria Exam
        ExamEntity exam = examService.findOrCreate(event.getExam());

        // 4️⃣ Resolve ou cria Session (única por student + exam)
        SessionEntity session;
        try{
        	session = sessionService.findOrCreate(student, exam);
        }catch (IllegalStateException e) {
        	return;
        }

        // 5️⃣ Monta EventEntity (histórico)
        EventEntity ent = new EventEntity();
        ent.setSession(session);
        ent.setType(event.getType());
        ent.setAction(event.getAction());
        ent.setCode(event.getCode());
        ent.setEventTime(event.getTime());      // tempo do cliente
        ent.setReceivedAt(receivedAt);          // tempo do servidor
        ent.setImage(event.getImage());

        // RAW = JSON completo do evento recebido
        try {
            ent.setRaw(mapper.writeValueAsString(event));
        } catch (Exception e) {
            ent.setRaw("{}"); }

        // 6️⃣ Persistência no MySQL (histórico imutável)
        eventRepository.save(ent);

        // 7️⃣ Atualiza atividade (Redis + SessionActivity)
        activityService.touch(session, ent);

        // 8️⃣ (opcional) Log em arquivo — se quiser manter
        saveEventToLog(event, student.getName(), exam.getCode());
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