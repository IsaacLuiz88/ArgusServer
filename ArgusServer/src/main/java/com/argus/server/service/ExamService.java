package com.argus.server.service;

import com.argus.server.bdmodel.ExamEntity;
import com.argus.server.repository.ExamRepository;
import org.springframework.stereotype.Service;

@Service
public class ExamService {

    private final ExamRepository repo;

    public ExamService(ExamRepository repo) {
        this.repo = repo;
    }

    public ExamEntity findOrCreate(String code) {
        return repo.findByCode(code)
                .orElseGet(() -> {
                    ExamEntity e = new ExamEntity();
                    e.setCode(code);
                    e.setName(code); // pode ajustar depois
                    return repo.save(e);
                });
    }
}
