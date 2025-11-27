package com.argus.server.repository;

import com.argus.server.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByExamAndStudentOrderByTimestampDesc(String exam, String student);
}
