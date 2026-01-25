package com.argus.server.repository;

import com.argus.server.bdmodel.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    Optional<ExamEntity> findByCode(String code);
}
