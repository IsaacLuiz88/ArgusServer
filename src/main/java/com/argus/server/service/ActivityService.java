package com.argus.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.argus.server.bdmodel.SessionEntity;
import com.argus.server.model.Event;
import com.argus.server.bdmodel.EventEntity;
import com.argus.server.bdmodel.SessionActivityEntity;
import com.argus.server.repository.SessionActivityRepository;

import java.time.LocalDateTime;

@Service
public class ActivityService {

    private final StringRedisTemplate redis;
    private final SessionActivityRepository repo;

    // argus.redis.enabled=false desliga o Redis por completo (ex.: hospedagem sem Redis).
    @Value("${argus.redis.enabled:true}")
    private boolean redisEnabled;

    // Se o Redis cair, pausa as tentativas por um tempo: senão cada evento ficaria
    // esperando o timeout de conexão e o servidor inteiro travaria junto.
    private static final long REDIS_RETRY_MS = 30_000;
    private volatile long redisDownUntil = 0;

    public ActivityService(
            StringRedisTemplate redis,
            SessionActivityRepository repo
    ) {
        this.redis = redis;
        this.repo = repo;
    }

    public void touch(SessionEntity session, Event eventDTO) {
        EventEntity dummy = new EventEntity();
        dummy.setType(eventDTO.getType());
        dummy.setAction(eventDTO.getAction());
        dummy.setReceivedAt(LocalDateTime.now());
        this.touch(session, dummy);
    }

    public void touch(SessionEntity session, EventEntity event) {
        // 🔹 Redis (rápido)
    	String key = "activity:" + session.getExam().getCode() + ":" + session.getStudent().getName();
        redisSet(key, String.valueOf(System.currentTimeMillis()));

        // 🔹 Banco (estado atual)
        SessionActivityEntity act =
                repo.findBySession(session)
                    .orElseGet(() -> {
                        SessionActivityEntity a = new SessionActivityEntity();
                        a.setSession(session);
                        return a;
                    });

        if(!"vision_frame".equalsIgnoreCase(event.getType())) {
        	act.setLastType(event.getType());
        	act.setLastAction(event.getAction());        	
        }
        act.setLastTimestamp(event.getReceivedAt());
        act.setUpdatedAt(LocalDateTime.now());
        repo.save(act);
    }

    public Long getLastActivity(SessionEntity session, String student, String exam) {
    	String key = "activity:" + session.getExam().getCode() + ":" + session.getStudent().getName();
        String value = redisGet(key);
        return value == null ? null : Long.parseLong(value);
    }

    public void reset(SessionEntity session) {
        String key = "activity:" + session.getExam().getCode() + ":" + session.getStudent().getName();
        redisDelete(key);
        repo.findBySession(session).ifPresent(repo::delete);
    }

    private boolean redisAvailable() {
        return redisEnabled && System.currentTimeMillis() >= redisDownUntil;
    }

    private void redisFailed(RuntimeException e) {
        redisDownUntil = System.currentTimeMillis() + REDIS_RETRY_MS;
        System.err.println("[REDIS] indisponível, pausando por " + (REDIS_RETRY_MS / 1000) + "s: " + e.getMessage());
    }

    private void redisSet(String key, String value) {
        if (!redisAvailable()) return;
        try {
            redis.opsForValue().set(key, value);
        } catch (RuntimeException e) {
            redisFailed(e);
        }
    }

    private String redisGet(String key) {
        if (!redisAvailable()) return null;
        try {
            return redis.opsForValue().get(key);
        } catch (RuntimeException e) {
            redisFailed(e);
            return null;
        }
    }

    private void redisDelete(String key) {
        if (!redisAvailable()) return;
        try {
            redis.delete(key);
        } catch (RuntimeException e) {
            redisFailed(e);
        }
    }
}
