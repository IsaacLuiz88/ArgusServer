package com.argus.server.service;

import com.argus.server.bdmodel.StudentEntity;
import com.argus.server.repository.StudentRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public Optional<StudentEntity> findByName(String name) {
        return repo.findByName(name);
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
