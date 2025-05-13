package com.ByteShield.HerbaWash.Services;

import com.ByteShield.HerbaWash.Entity.SyncAudit;
import com.ByteShield.HerbaWash.Entity.UserEntity;
import com.ByteShield.HerbaWash.Repository.SyncAuditRepositry;
import com.ByteShield.HerbaWash.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {
    private final WebClient keyclaokWebClient;
    private final UserRepository userRepository;
    private final SyncAuditRepositry syncAuditRepositry;

    @Value("${keycloak.admin.username}")
    private String adminUsername;
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value("${keycloak.client.id}")
    private String clientID;
    @Value("${keycloak.client.secret}")
    private String clientSecret;

    @Value("${keycloak.realm}")
    private String realm;

    private String getAdminToken(){
        return keyclaokWebClient.post()
                .uri("/realms/"+realm+"/protocol/openid-connect/token")
                .header("Content-Type","application/x-www-form-urlencoded")
                .bodyValue("client_id="+clientID+"&client_secret="+clientSecret+"&username="+adminUsername+"&password="+adminPassword+"&grant_type=password")
                .retrieve()
                .bodyToMono(Map.class)
                .map(tokenMap->tokenMap.get("access_token").toString())
                .block();
    }


    public void syncUser(String triggeredBy){
        int count=0;
        SyncAudit.SyncAuditBuilder audit=SyncAudit.builder()
                .timestamp(LocalDateTime.now())
                .triggerBy(triggeredBy);
        try {


            String token = getAdminToken();
            log.info(token);

            List<Map<String, Object>> users = keyclaokWebClient.get()
                    .uri("/admin/realms/" + realm + "/users")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (users != null)
                log.info("Users are there");


            if (users.isEmpty()) {
                log.warn("No user fetched from keycloak");
                return;
            }
            for (Map<String, Object> user : users) {
                String userId = user.get("id").toString();
                log.info(userId);
                String username = user.get("username").toString();
                String email = user.get("email") != null ? user.get("email").toString() : "";
                Set<String> clientRoles = new HashSet<>();
                Set<String> realmRoles = new HashSet<>();

                Map<String, Object> roleMappings = keyclaokWebClient.get()
                        .uri("/admin/realms/" + realm + "/users/" + userId + "/role-mappings")
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                List<Map<String, Object>> realmMappings = (List<Map<String, Object>>) roleMappings.get("realmMappings");
                if (realmMappings != null) {
                    for (Map<String, Object> role : realmMappings) {
                        realmRoles.add(role.get("name").toString());
                    }
                }

                Map<String, Object> clientMappings = (Map<String, Object>) roleMappings.get("clientMappings");
                if (clientMappings != null) {
                    for (Object clientMapObj : clientMappings.values()) {
                        Map<String, Object> clientMap = (Map<String, Object>) clientMapObj;
                        List<Map<String, Object>> roles = (List<Map<String, Object>>) clientMap.get("mappings");
                        for (Map<String, Object> role : roles) {
                            clientRoles.add(role.get("name").toString());
                        }
                    }
                }
                List<Map<String, Object>> sessions = keyclaokWebClient.get()
                        .uri("/admin/realms/" + realm + "/users/" + userId + "/sessions")
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(List.class)
                        .block();
                int sessionCount = (sessions != null) ? sessions.size() : 0;


                UserEntity userEntity = UserEntity.builder()
                        .userId(userId)
                        .username(username)
                        .email(email)
                        .clientRoles(clientRoles)
                        .realmRoles(realmRoles)
                        .lastSyncAt(LocalDateTime.now())
                        .sessionCount(sessionCount)

                        .build();
                userRepository.save(userEntity);
                count+=1;
                audit.id(UUID.randomUUID());
                audit.status("SUCCEEDED");
                audit.userSynced(count);
                log.info("Synced User:{} with {} realmroles and {} clientroles", username, realmRoles.size(), clientRoles.size());



            }
            log.info("User Sync Successfully");
        }catch (Exception e){
            log.error("Sync Failed:{}",e.getMessage(),e);
            audit.status("FAILURE");
            audit.userSynced(count);
            audit.errorDetails(e.getMessage());


        }
        syncAuditRepositry.save(audit.build());

    }
    @Scheduled(fixedRate = 30*60*1000)
    public void scheduledSync(){
        syncUser("SCHEDULED");
    }


}
