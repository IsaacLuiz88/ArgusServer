package com.argus.server.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private final StringRedisTemplate redis;

    public ActivityService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void touch(String student, String exam) {
        String key = "activity:" + exam + ":" + student;
        redis.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
    }

    public Long getLastActivity(String student, String exam) {
        String key = "activity:" + exam + ":" + student;
        String value = redis.opsForValue().get(key);
        return value == null ? null : Long.parseLong(value);
    }
}
