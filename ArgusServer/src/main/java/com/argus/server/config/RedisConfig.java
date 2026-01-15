package com.argus.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public StringRedisTemplate redisTemplate() {
        return new StringRedisTemplate(connectionFactory());
    }
}
