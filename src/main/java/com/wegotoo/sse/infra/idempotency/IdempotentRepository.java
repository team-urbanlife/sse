package com.wegotoo.sse.infra.idempotency;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotentRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public String findByMessageId(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean setIfAbsent(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMinutes(1));
    }

}
