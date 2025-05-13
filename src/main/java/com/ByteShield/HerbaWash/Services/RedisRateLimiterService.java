package com.ByteShield.HerbaWash.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRateLimiterService {
    private final StringRedisTemplate redisTemplate;
    private final static int DEFAULT_LIMIT=100;
    private final static int DEFAULT_WINDOW=60;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public boolean isAllowed(HttpServletRequest request){
        String ip=request.getRemoteAddr();
        String path=request.getRequestURI();
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String userId=null;
        String role=extractUserRoleOrPlan(request);
        if (auth!=null && auth.isAuthenticated()){
            userId=auth.getName();

        }
        String key = String.format("rate_limit:%s:%s:%s", path,(role!=null?role:"anonymous"), (userId != null ? userId : ip));
        System.out.println("userid :"+userId+" role "+role);
        int limit=getLimitForPath(request.getRequestURI(),role);
        int windowSeconds=getWindowForPath(request.getRequestURI(),role);
        System.out.println("Limit: "+limit+" windows "+windowSeconds);
        long current=redisTemplate.opsForValue().increment(key);
        if (current==1){
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        // ⬇️ Custom Prometheus metrics

        meterRegistry.counter("rate.limit.check", Tags.of(
                "path", Objects.toString(path, "unknown"),
                "user", Objects.toString(userId, "unknown"),
                "role", Objects.toString(role, "Anonymous")
        )).increment();


        // live gauge
        Gauge.builder("rate_limit_current_usage", () -> current)
                .tags("path", Optional.ofNullable(path).orElse("unknown"),
                        "userId", Optional.ofNullable(userId).orElse("unknown use"),
                        "role", Optional.ofNullable(role).orElse("anonymous"))
                .register(meterRegistry);

        if (current > limit) {
            log.warn("Rate limit exceeded for key: {}, count: {}", key, current);
        }

        return current<=limit;
    }

    private int getLimitForPath(String path,String role){
        if(path.startsWith("/api/public/login"))
            return 10;
        if (path.startsWith("/api/admin/sync-users")){
            return switch (role){
                case "ADMIN"->500;
                case "ROLE_USER"->50;
                default -> 10;
            };
        }
        if (path.startsWith("/api/public"))
            return 200;
        return DEFAULT_LIMIT;
    }
    private int getWindowForPath(String path, String role) {
        if (path.startsWith("/api/admin/sync-users")) {
            return switch (role) {
                case "ROLE_ADMIN" -> 60;
                case "ROLE_USER" -> 120;
                default -> 180;
            };
        }
        if (path.startsWith("/api/public")) {
            return 30;
        }
        return DEFAULT_WINDOW;
    }
    private String extractUserRoleOrPlan(HttpServletRequest request) {
        try {
            String authorizationHeader=request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
                log.warn("Authorization header is missing or invalid");
                return "anonymous";
            }
            String token = request.getHeader("Authorization").substring(7) ;
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

                // Check realm_access for roles
                if (claims.containsKey("realm_access")) {
                    Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    System.out.println(roles);

                    // Extract the roles properly
                    if (roles.contains("ADMIN")) return "ADMIN";
                    if (roles.contains("ROLE_USER")) return "ROLE_USER";
                }

                // Fallback for custom plan claim
                if (claims.containsKey("plan")) {
                    return (String) claims.get("plan");
                }
            }
        } catch (Exception e) {
            log.warn("Couldn't extract role/plan from Keycloak token", e);
        }
        return "anonymous";
    }




}
