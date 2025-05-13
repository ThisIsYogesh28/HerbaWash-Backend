package com.ByteShield.HerbaWash.Controller;

import com.ByteShield.HerbaWash.DTO.SessionAnalyticsDTO;
import com.ByteShield.HerbaWash.Entity.LoginHistoryEntity;
import com.ByteShield.HerbaWash.Services.KeycloakSessionSyncService;
import com.ByteShield.HerbaWash.Services.LoginHistoryService;
import com.ByteShield.HerbaWash.Services.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final RedisSessionService redisSessionService;
    private final KeycloakSessionSyncService keycloakSessionSyncService;
    private final LoginHistoryService loginHistoryService;

    @GetMapping("/all")
    public ResponseEntity<Map<String,Map<String,Object>>> getAllUserSessions(){
        Map<String,Map<String,Object>> sessions=redisSessionService.getAllSessions();
        return ResponseEntity.ok(sessions);

    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String,Object>> getUserSession(@PathVariable String userId){
        Map<String,Object> sessions=redisSessionService.getUsersSessions(userId);
        if (sessions!=null){
            return ResponseEntity.ok(sessions);
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearUserSessions(@PathVariable String userId){
        redisSessionService.clearSession(userId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/logout/{userId}/{sessionId}")
    public String logoutSession(@PathVariable String userId,@PathVariable String sessionId){
        try {
            keycloakSessionSyncService.logoutSessionFromKeycloak(sessionId);
            redisSessionService.removeSession(userId,sessionId);
            return "Session "+sessionId+" logout successfully";
        }catch (Exception e){
            log.error(" Failed to logout session {} for user {}: {}", sessionId, userId, e.getMessage());
            return " Failed to logout session: " + e.getMessage();
        }
    }
    @DeleteMapping("/logout-all/{userId}")
    public String logoutAllSessions(@PathVariable String userId){
        try {
            keycloakSessionSyncService.logoutAllSessionsFromKeycloak(userId);
            redisSessionService.clearSession(userId);
            log.info("Successfully logged out all session of user {}",userId);
            return "All sessions of user " +userId+" logged out successfully";
        }catch (Exception e){
            log.error("Failed to logout all sessions of the user {}: {}",userId,e.getMessage());

            return "Failed to logout all sessions of user "+e.getMessage();
        }
    }
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Map<String,String>>> getLoginHistory(@PathVariable String userId){
        List<Map<String,String>> history=loginHistoryService.getLoginHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/refresh-ttl/{userId}")
    public String refreshSessionTTl(@PathVariable String userId){
        try {
            redisSessionService.refreshSessionTTL(userId);
            log.info("Session TTl is refreshed for user {}",userId);
            return "Successfully Session TTl is refreshed for user "+userId;

        }catch (Exception e){
            log.error("failed to refresh TTl of user {}:{}",userId,e.getMessage());
            return "Failed Session TTl is refreshed for user "+userId;
        }
    }
    @GetMapping("/analytics/{userId}")
    public ResponseEntity<SessionAnalyticsDTO> getSessionAnalytics(@PathVariable String userId) {
        SessionAnalyticsDTO analytics = redisSessionService.getSessionAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }




}
