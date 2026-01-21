package com.argus.server.service;

import com.argus.server.bdmodel.*;
import com.argus.server.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository repo;

    public SessionService(SessionRepository repo) {
        this.repo = repo;
    }

    public SessionEntity findOrCreate(StudentEntity student, ExamEntity exam) {

        return repo.findActiveSession(student, exam)
                .orElseGet(() -> {
                    SessionEntity s = new SessionEntity();
                    s.setStudent(student);
                    s.setExam(exam);
                    s.setSessionUuid(UUID.randomUUID().toString());
                    s.setStartedAt(LocalDateTime.now());
                    s.setStatus("ACTIVE");
                    return repo.save(s);
                });
    }

    public void end(SessionEntity session) {
        session.setStatus("FINISHED");
        session.setEndedAt(LocalDateTime.now());
        repo.save(session);
    }

    public void endByStudent(String student) {
        repo.findActiveByStudentName(student)
            .ifPresent(this::end);
    }
}
