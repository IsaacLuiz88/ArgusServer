package com.argus.server.repository;

import com.argus.server.bdmodel.EventEntity;
import com.argus.server.bdmodel.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findBySessionOrderByReceivedAtDesc(SessionEntity session);
}
