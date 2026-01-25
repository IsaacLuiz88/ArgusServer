package com.argus.server.service;

import com.argus.server.bdmodel.*;
import com.argus.server.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository repo;

    public SessionService(SessionRepository repo) {
        this.repo = repo;
    }

    public SessionEntity findOrCreate(StudentEntity student, ExamEntity exam) {

    	// Verifica se existe sessão ativa
        Optional<SessionEntity> active = repo.findActiveSession(student, exam);

        if(active.isPresent()) {
        	 SessionEntity s = active.get(); // mesmo estudante/exame, retorna a sessão

        	// 🔴 Sessão já encerrada → IGNORA evento
             if ("FINISHED".equals(s.getStatus())) {
                 throw new IllegalStateException("Sessão encerrada. Evento ignorado.");
             }
             return s;
        }

        // Encerra qualquer sessão ativa de outros estudantes
        //repo.findFirstByStatus("ACTIVE").ifPresent(this::end);

        // Cria nova sessão
        SessionEntity s = new SessionEntity();
        s.setStudent(student);
        s.setExam(exam);
        s.setSessionUuid(student.getName() + "_" + exam.getName() + "_" + UUID.randomUUID());
        s.setStartedAt(LocalDateTime.now());
        s.setStatus("ACTIVE");
        return repo.save(s);
    }

    public void end(SessionEntity session) {
        session.setStatus("FINISHED");
        session.setEndedAt(LocalDateTime.now());
        repo.save(session);
    }

    public Optional<SessionEntity> findFirstActive() {
        return repo.findFirstByStatus("ACTIVE");
    }

    public SessionEntity create(StudentEntity student, ExamEntity exam) {
        SessionEntity s = new SessionEntity();
        s.setStudent(student);
        s.setExam(exam);
        s.setSessionUuid(
            student.getName() + "-" +
            exam.getCode() + "-" +
            UUID.randomUUID()
        );
        s.setStartedAt(LocalDateTime.now());
        s.setStatus("ACTIVE");
        return repo.save(s);
    }

    public Optional<SessionEntity> findActiveByStudentName(String student) {
        return repo.findActiveByStudentName(student);
    }

    public void endByStudent(String student) {
        repo.findActiveByStudentName(student)
            .ifPresent(this::end);
    }

//    public void endBySession(String sessionUuid) {
//        SessionEntity s = repo.findBySessionUuid(sessionUuid)
//            .orElseThrow();
//
//        s.setStatus("FINISHED");
//        s.setEndedAt(LocalDateTime.now());
//        repo.save(s);
//    }
}
