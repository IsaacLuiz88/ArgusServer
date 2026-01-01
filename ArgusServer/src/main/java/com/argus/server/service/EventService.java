package com.argus.server.service;

import org.springframework.stereotype.Service;
import com.argus.server.model.EventEntity;
import com.argus.server.repository.EventRepository;

@Service
public class EventService {

    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    public EventEntity save(EventEntity e) {
        return repo.save(e);
    }
}