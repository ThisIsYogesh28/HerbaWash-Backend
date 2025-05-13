package com.ByteShield.HerbaWash.Services;

import com.ByteShield.HerbaWash.Entity.UserSessionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakSessionSyncService {

    private final WebClient keycloakWebClient;
    private final KeycloakTokenService tokenService;
    private final RedisSessionService redisSessionService;
    @Value("${keycloak.realm}")
    private  String realm;

    @Scheduled(fixedDelay = 300000)
    public void syncAllSessions(){
        log.info("🔄 Starting Keycloak session sync...");
        try {
            String token=tokenService.fetchAccessToken();
            System.out.println(token);
            String userUrl="/admin/realms/" + realm + "/users";

            List<Map<String,Object>> users= keycloakWebClient.get()
                    .uri(userUrl)
                    .header("Authorization","Bearer "+token)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (users!=null){
                for (Map<String,Object> user:users){
                    String userId=(String) user.get("id");
                    String username=(String) user.get("username");
                    syncUserSessions(userId,username,token);
                }
            }
            log.info("Session sync completed.");
        }catch (Exception e){
            log.error("Session sync failed: {}", e.getMessage());

        }
    }

        private void syncUserSessions(String userId, String username, String token) {
            String url = "/admin/realms/" + realm + "/users/" + userId + "/sessions";

            try {
                List<Map<String, Object>> sessions = keycloakWebClient.get()
                        .uri(url)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(List.class)
                        .block();
                if (sessions != null && !sessions.isEmpty()) {
                    List<UserSessionInfo> sessionInfoList = new ArrayList<>();
                    long now=System.currentTimeMillis();
                    long timeoutMillis=30*60*1000;
                    for (Map<String, Object> session : sessions) {
                        Object sessionIdObj = session.get("id");
                        Object startObj = session.get("start");
                        Object lastAccessObj = session.get("lastAccess");

                        String sessionId = sessionIdObj != null ? String.valueOf(sessionIdObj) : null;
                        if (sessionId == null) {
                            log.warn("⚠️ Null session ID for user {}", username);
                            continue;
                        }

                        long createdAt = startObj != null ? ((Number) startObj).longValue() : 0L;
                        long lastAccess = lastAccessObj != null ? ((Number) lastAccessObj).longValue() : 0L;
                        String status;
                        if (now-lastAccess*1000>timeoutMillis)
                            status="EXPIRED";
                        else status="ACTIVE";

                        UserSessionInfo sessionInfo = new UserSessionInfo();
                        sessionInfo.setSessionId(sessionId);
                        sessionInfo.setIpAddress((String) session.get("ipAddress"));
                        sessionInfo.setCreatedAt(createdAt);
                        sessionInfo.setLastAccess(lastAccess);
                        sessionInfo.setStatus(status);

                        sessionInfoList.add(sessionInfo);
                    }
                    redisSessionService.saveUserSessions(userId, username, sessionInfoList);
                    log.info(" Synced {} sessions for user {}", sessionInfoList.size(), username);
                } else {
                    redisSessionService.clearSession(userId);
                    log.info(" No sessions found for user {}", username);
                }
            } catch (Exception e) {
                log.warn("Failed to sync sessions for user {}: {}", username, e.getMessage());
            }
        }
        public void logoutSessionFromKeycloak(String sessionId){
            try {
                String token= tokenService.fetchAccessToken();
                String url="/admin/realms/"+realm+"/sessions/"+sessionId;
                keycloakWebClient.delete()
                        .uri(url)
                        .header("Authorization","Bearer "+token)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                log.info("Successfully logged out session {}",sessionId);
            }catch (Exception e){
                log.error("Failed to logot session {} from keycloak{}",sessionId,e.getMessage());
                throw new RuntimeException("Failed to logout session from keycloak");
            }
        }
        public void logoutAllSessionsFromKeycloak(String userId){
            try {
                String token=tokenService.fetchAccessToken();
                String url="/admin/realms/"+realm+"/users/"+userId+"/logout";
                keycloakWebClient.post()
                        .uri(url)
                        .header("Authorization","Bearer "+token)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                log.info("Successfully logout all sessions of user {}",userId);
            }catch (Exception e){
                    log.error("failed to logout all sessions of user {} :{}",userId,e.getMessage());
                    throw new RuntimeException("Failed to logout all sessions from keycloak");
            }
        }


    }


