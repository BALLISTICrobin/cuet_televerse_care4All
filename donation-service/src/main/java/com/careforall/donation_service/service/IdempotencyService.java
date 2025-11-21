package com.careforall.donation_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    // Returns true if key is new (lock acquired), false if already exists
    public boolean process(String key) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5));
        return Boolean.TRUE.equals(success);
    }

    public void complete(String key) {
        redisTemplate.opsForValue().set(key, "COMPLETED", Duration.ofHours(24));
    }
}