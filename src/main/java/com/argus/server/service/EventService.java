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
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    private void handlePluginScan(Event event) {
    	if (event.getPlugins() == null || event.getPlugins().isEmpty()) return;

        String allPlugins = String.join(" ", event.getPlugins()).toLowerCase();

        boolean suspicious = allPlugins.contains("copilot") ||
        		allPlugins.contains("openai") ||
                allPlugins.contains("tabnine") || 
                allPlugins.contains("codeium") ||
                allPlugins.contains("blackbox") ||
                allPlugins.contains("amazon.q") ||
                allPlugins.contains("chatgpt");

        if (suspicious) {
            System.out.println("[ARGUS ALERT] Plugin suspeito detectado: " + event.getStudent());
            event.setType("plugin_suspect");
            event.setAction("IA_PLUGIN_DETECTED");
        }
    }

    public boolean handleIncomingEvent(Event event, String ip) throws Exception {
    	try{
    	// 1. Busque o aluno e a prova primeiro
    	StudentEntity student = studentService.findByName(event.getStudent())
        	    .orElseThrow(() -> new IllegalStateException("Aluno não cadastrado no sistema"));

        ExamEntity exam = examService.findByCode(event.getExam())
        	    .orElseThrow(() -> new IllegalStateException("Prova não cadastrada ou não existe"));

     // 2. Busque a sessão ativa
        SessionEntity session = null;
        if (event.getSession() != null && !event.getSession().isBlank()) {
            session = sessionService.findByUuid(event.getSession()).orElse(null);
        }
        if (session == null) {
            session = sessionService.findActiveSession(student, exam).orElse(null);
        }
        //SessionEntity session = sessionService.findActiveSession(student, exam).orElse(null);

        // 3. Validação: Se não tem sessão, ignoramos (especialmente heartbeats)
        if (session == null) {
            if ("heartbeat".equalsIgnoreCase(event.getType())) return false;
            System.out.println("[EVENT] Ignorado - Sem sessão ativa para: " + event.getStudent());
            return false;
        }

     // 4. PREENCHIMENTO OBRIGATÓRIO (A ponte para o Dashboard)
        event.setSession(session.getSessionUuid());
        event.setIp(ip);

     // 5. Fluxo Específico: HEARTBEAT
        if ("heartbeat".equalsIgnoreCase(event.getType())) {
        	activityService.touch(session, event); // Atualiza Redis/Atividade
            return true;
        }

      // 6. Processa outros tipos de evento (plugin_scan, etc)
    	if ("plugin_scan".equalsIgnoreCase(event.getType())) {handlePluginScan(event);}

      // Sincroniza tempos de prova para o Dashboard
    	if (session.getStartedAt() != null) {
            long startMillis = session.getStartedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            event.setStudentStart(startMillis);}

        if (exam.getStartedAt() != null) {
            long examStartMillis = exam.getStartedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            event.setExamStart(examStartMillis);}

      //7. PERSISTÊNCIA NO BANCO (MySQL)
        EventEntity ent = new EventEntity();
        ent.setSession(session);
        ent.setType(event.getType());
        ent.setAction(event.getAction());
        ent.setReceivedAt(LocalDateTime.now());
        ent.setIp(ip);

        try {
        ent.setRaw(mapper.writeValueAsString(event));
        }catch(Exception e){
        	ent.setRaw("{}");
        }

        eventRepository.save(ent);
     // 5. Atualiza o estado de atividade (Redis/Cache)
        activityService.touch(session, ent);
        
        saveEventToLog(event, student.getName(), exam.getCode());
        return true;
    	} catch (Exception e) {
            e.printStackTrace(); // Isso vai mostrar o erro real no console do Eclipse/IntelliJ
            return false;
        }
     // 3. Se achou a sessão, anexe o UUID ao evento (MESMO QUE SEJA HEARTBEAT)
//        if (session != null) {
//            event.setSession(session.getSessionUuid());
//        }else {
//            // Se NÃO tem sessão e é apenas um heartbeat, ignora silenciosamente
//            if ("heartbeat".equalsIgnoreCase(event.getType())) return false;
//            
//            // Se NÃO tem sessão e é um evento real (focus, click), loga o aviso
//            System.out.println("[EVENT] Ignorado - Sem sessão ativa para: " + event.getStudent());
//            return false;
//        }
        //4 Tratamento ÚNICO de Heartbeat
        

     // 4. Se for heartbeat, atualiza atividade e para aqui
//        if ("heartbeat".equalsIgnoreCase(event.getType())) {
//        	EventEntity heartbeatEvent = new EventEntity();
//            heartbeatEvent.setType("heartbeat");
//            heartbeatEvent.setAction("ALIVE");
//            heartbeatEvent.setReceivedAt(LocalDateTime.now());
//            activityService.touch(session, heartbeatEvent);
//            event.setIp(session.getSessionUuid());
//            event.setIp(ip);
//            return true; 
//        }
//     // 6. Lógica de IP e Sincronização (session já é garantido não ser null aqui)
//        if (session.getIpAddress() == null) {
//            session.setIpAddress(ip);
//            sessionService.save(session);
//        }
//        // RAW = JSON completo do evento recebido
//        try {
//        	if ("vision_frame".equalsIgnoreCase(event.getType())) {
//                Event clone = new Event();
//                clone.setType(event.getType());
//                clone.setStudent(event.getStudent());
//                clone.setExam(event.getExam());
//                clone.setTimestamp(event.getTimestamp());
//                ent.setRaw(mapper.writeValueAsString(clone));
//            } else {
//                ent.setRaw(mapper.writeValueAsString(event));
//            }
//        } catch (Exception e) {
//            ent.setRaw("{}"); }
//        // 6️⃣ Persistência no MySQL
//        eventRepository.save(ent);
//        // 7️⃣ Atualiza estado de atividade (Redis p/ tempo real + MySQL p/ histórico)
//        activityService.touch(session, ent);
//        // 8️⃣ Log físico para auditoria
//        saveEventToLog(event, student.getName(), exam.getCode());
//        return true;
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
