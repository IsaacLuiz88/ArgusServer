package com.argus.server.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.argus.server.bdmodel.SessionEntity;
import com.argus.server.bdmodel.EventEntity;
import com.argus.server.bdmodel.SessionActivityEntity;
import com.argus.server.repository.SessionActivityRepository;

import java.time.LocalDateTime;

@Service
public class ActivityService {

    private final StringRedisTemplate redis;
    private final SessionActivityRepository repo;

    public ActivityService(
            StringRedisTemplate redis,
            SessionActivityRepository repo
    ) {
        this.redis = redis;
        this.repo = repo;
    }

    public void touch(SessionEntity session, EventEntity event) {

        // 🔹 Redis (rápido)
        String key = "activity:session:" + session.getId();
        redis.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));

        // 🔹 Banco (estado atual)
        SessionActivityEntity act =
                repo.findBySession(session)
                    .orElseGet(() -> {
                        SessionActivityEntity a = new SessionActivityEntity();
                        a.setSession(session);
                        return a;
                    });

        act.setLastType(event.getType());
        act.setLastAction(event.getAction());
        act.setLastTimestamp(event.getReceivedAt());
        act.setLastImage(event.getImage());
        act.setUpdatedAt(LocalDateTime.now());

        repo.save(act);
    }

    public Long getLastActivity(String student, String exam) {
        String key = "activity:" + exam + ":" + student;
        String value = redis.opsForValue().get(key);
        return value == null ? null : Long.parseLong(value);
    }
}
