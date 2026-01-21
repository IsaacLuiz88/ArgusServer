package com.argus.server.service;

import com.argus.server.bdmodel.StudentEntity;
import com.argus.server.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public StudentEntity findOrCreate(String name) {
        return repo.findByName(name)
                .orElseGet(() -> {
                    StudentEntity s = new StudentEntity();
                    s.setName(name);
                    return repo.save(s);
                });
    }
}
