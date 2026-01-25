package com.argus.server.repository;

import com.argus.server.bdmodel.SessionActivityEntity;
import com.argus.server.bdmodel.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionActivityRepository
        extends JpaRepository<SessionActivityEntity, Long> {

    Optional<SessionActivityEntity> findBySession(SessionEntity session);
}
