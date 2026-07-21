package com.argus.server.service;

import com.argus.server.bdmodel.*;
import com.argus.server.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

	private final SessionRepository repo;
	private final ActivityService activityService;

	public SessionService(SessionRepository repo, ActivityService activityService) {
		this.repo = repo;
		this.activityService = activityService;
	}

	public SessionEntity create(StudentEntity student, ExamEntity exam) {
		SessionEntity s = new SessionEntity();
        s.setStudent(student);
        s.setExam(exam);
        
        // PADRONIZADO: nome_prova_uuid
        String uuid = String.format("%s_%s_%s", 
            student.getName(), 
            exam.getCode(), 
            UUID.randomUUID().toString().substring(0, 8)); // UUID curto para facilitar log

        s.setSessionUuid(uuid);
        s.setStartedAt(LocalDateTime.now());
        s.setStatus("ACTIVE");

        SessionEntity saved = repo.save(s);
        activityService.reset(saved);
        return saved;
	}

	public SessionEntity findOrCreate(StudentEntity student, ExamEntity exam) {
		return repo.findActiveSession(student, exam)
                .orElseGet(() -> create(student, exam));
	}

	public SessionEntity save(SessionEntity session) {
		return repo.save(session);
	}

	public void end(SessionEntity session) {
		session.setStatus("FINISHED");
		session.setEndedAt(LocalDateTime.now());
		repo.save(session);
	}

	public Optional<SessionEntity> findByUuid(String uuid) {
	    return repo.findBySessionUuid(uuid);
	}

	public Optional<SessionEntity> findFirstActive() {
		return repo.findFirstByStatus("ACTIVE");
	}

	public Optional<SessionEntity> findActiveByStudentName(String student) {
		return repo.findActiveByStudentName(student);
	}

	public void endByStudent(String student) {
		repo.findActiveByStudentName(student).ifPresent(this::end);
	}

	public List<SessionEntity> findAllActive() {
	    return repo.findAllActive();
	}

	public Optional<SessionEntity> findActiveSession(StudentEntity student, ExamEntity exam) {
	    return repo.findActiveSession(student, exam);
	}
}
