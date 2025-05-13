package com.ByteShield.HerbaWash.Services;

import com.ByteShield.HerbaWash.Entity.TokenRevocationLog;
import com.ByteShield.HerbaWash.Repository.TokenRevocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRevocationService {
    private final StringRedisTemplate redisTemplate;
    private final TokenRevocationRepository revocationRepository;
    private static final String REVOKED_TOKEN_PREFIX="revoked_token";
    public void revokeToken(String token,String userId){
        String redisKey=REVOKED_TOKEN_PREFIX+userId+":"+token;
        redisTemplate.opsForValue().set(redisKey,"revoked",30,TimeUnit.DAYS);
        TokenRevocationLog logs=new TokenRevocationLog();
        logs.setUserId(userId);
        logs.setTokenHash(String.valueOf(token.hashCode()));
        logs.setRevokedAt(LocalDateTime.now());
        revocationRepository.save(logs);
        log.info("Token revoked for userId={} at {}", userId, logs.getRevokedAt());

    }

    public boolean isTokenRevoked(String token ,String userId){
        String redisKey=REVOKED_TOKEN_PREFIX+userId+":"+token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey+token));
    }
}
