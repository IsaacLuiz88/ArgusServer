package com.argus.server.service;

import com.argus.server.bdmodel.ExamEntity;
import com.argus.server.repository.ExamRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ExamService {

    private final ExamRepository repo;

    public ExamService(ExamRepository repo) {
        this.repo = repo;
    }
    
    public Optional<ExamEntity> findByCode(String code) {
        return repo.findByCode(code);
    }

    public ExamEntity findOrCreate(String code) {
        return repo.findByCode(code)
                .orElseGet(() -> {
                    ExamEntity e = new ExamEntity();
                    e.setCode(code);
                    return repo.save(e);
                });
    }

    public ExamEntity startExam(String code) {
        ExamEntity exam = findOrCreate(code);
        exam.setStartedAt(LocalDateTime.now());
        return repo.save(exam);
    }

    // Encerra a prova definitivamente: nenhuma nova sessão pode ser criada
    // para este código depois disso (ver SessionController.start).
    public ExamEntity closeExam(String code) {
        ExamEntity exam = findOrCreate(code);
        exam.setEndedAt(LocalDateTime.now());
        return repo.save(exam);
    }
}
