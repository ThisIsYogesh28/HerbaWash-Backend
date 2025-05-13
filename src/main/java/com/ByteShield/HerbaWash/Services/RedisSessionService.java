package com.ByteShield.HerbaWash.Services;

import com.ByteShield.HerbaWash.DTO.SessionAnalyticsDTO;
import com.ByteShield.HerbaWash.Entity.UserSessionInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSessionService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String PREFIX = "session:user:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    public void saveUserSessions(String userId, String username, List<UserSessionInfo> sessions) {
        try {
            if (sessions == null || sessions.isEmpty()) {
                log.warn("No sessions provided for user {}", userId);
                return;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("username", username);
            userData.put("sessionCount", sessions.size());
            userData.put("lastLogin", sessions.get(0).getCreatedAt());
            userData.put("sessions", sessions);
            String json = objectMapper.writeValueAsString(userData);
            redisTemplate.opsForValue().set(PREFIX + userId, json, SESSION_TTL);

        } catch (Exception e) {
            log.warn("could not retrieve session of user {} : {} ", userId, e.getMessage());
        }
    }

    public Map<String, Object> getUsersSessions(String userId) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + userId);
            if (json != null)
                return objectMapper.readValue(json, new TypeReference<>() {
                });

        } catch (Exception e) {
            log.warn("Could not retrieve session for user {}: {}", userId, e.getMessage());
        }
        return null;
    }

    public void clearSession(String userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    public Map<String, Map<String, Object>> getAllSessions() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        try {

            redisTemplate.keys(PREFIX + "*").forEach(key -> {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    try {
                        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
                        });
                        result.put(key.replace(PREFIX, ""), data);
                    } catch (Exception e) {
                        log.warn("Error parsing session data for key {}: {}", key, e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error fetching all session data: {}", e.getMessage());
        }
        return result;
    }

    public void removeSession(String userId, String sessionId) {
        String key = PREFIX + userId;
        String userJson = redisTemplate.opsForValue().get(key);
        if (userJson != null) {
            try {
                // Deserialize full user data
                Map<String, Object> userData = objectMapper.readValue(userJson, new TypeReference<>() {
                });

                // Extract and modify sessions
                List<UserSessionInfo> sessionList = objectMapper.convertValue(userData.get("sessions"), new TypeReference<List<UserSessionInfo>>() {
                });
                sessionList.removeIf(session -> session.getSessionId().equals(sessionId));

                // Update session list and sessionCount
                userData.put("sessions", sessionList);
                userData.put("sessionCount", sessionList.size());

                // Update lastLogin if needed
                if (!sessionList.isEmpty()) {
                    userData.put("lastLogin", sessionList.get(0).getCreatedAt());
                } else {
                    userData.put("lastLogin", null);
                }

                // Save back
                String updatedJson = objectMapper.writeValueAsString(userData);
                redisTemplate.opsForValue().set(key, updatedJson);

                log.info(" Removed session {} for user {}", sessionId, userId);
            } catch (Exception e) {
                log.error(" Failed to remove session {} for user {}: {}", sessionId, userId, e.getMessage());
            }
        }
    }

    public void refreshSessionTTL(String userId) {
        String key = PREFIX + userId;
        Boolean exists = redisTemplate.hasKey(key);
        if (exists) {
            redisTemplate.expire(key, SESSION_TTL);
            log.info("Refreshed TTL for user {} ", userId);
        }
    }

    public SessionAnalyticsDTO getSessionAnalytics(String userId) {
        try{
            String key = PREFIX + userId;
            Map<Object, Object> sessionMap = redisTemplate.opsForHash().entries(key);
            int total = sessionMap.size();
            int active = 0;
            int expired = 0;
            Date lastActive = null;

            for (Object values : sessionMap.values()) {

                if (values instanceof Map) {
                    Map<?, ?> session = (Map<?, ?>) values;


                    Boolean isActive = (Boolean) session.get("active");
                    Long lastAccessTime = (Long) session.get("lastAccessTime");
                    if (isActive != null && isActive) {
                        active++;
                        if (lastAccessTime != null) {
                            Date lastTime = new Date(lastAccessTime);
                            if (lastActive == null || lastTime.after(lastActive))
                                lastActive = lastTime;
                        }


                    } else expired++;

                }
            }
            return new SessionAnalyticsDTO(userId, total, active, expired, lastActive);
        }catch (Exception e){
            log.error("failed to get session analytics for user {} :{}",userId,e.getMessage());
            return new SessionAnalyticsDTO(userId,0,0,0,null);
        }

    }
}
