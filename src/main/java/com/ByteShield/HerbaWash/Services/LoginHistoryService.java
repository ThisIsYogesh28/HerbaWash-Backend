package com.ByteShield.HerbaWash.Services;

import com.ByteShield.HerbaWash.Entity.UserSessionInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {
    @Autowired
    private final RedisTemplate<String,Object> redisTemplate;
    private static final int MAX_HISTORY=5;
    private final String PREFIX="user:login:history:";
    private final String METADATA_PREFIX="user:metadata:";
    private final String SESSION_PREFIX="session:user:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Kolkata"));

    public void recordLogin(String userid,String ipAddress,String deviceType){
        String key=PREFIX+userid;
//        LoginHistoryEntity entry=new LoginHistoryEntity();
//        entry.setIpAddress(ipAddress);
//        entry.setDeviceType(deviceType);
//        entry.setTimestamp(System.currentTimeMillis());
//        List<LoginHistoryEntity> history=getLoginHistory(userid);
//        if (history==null)
//            history=new ArrayList<>();
//        history.addFirst(entry);
//        if (history.size()>MAX_HISTORY)
//            history.subList(0,MAX_HISTORY);
//        redisTemplate.opsForValue().set(key,history);
        long timestamp= Instant.now().getEpochSecond();
        String value=timestamp+"|"+ipAddress+"|"+deviceType;
        redisTemplate.opsForZSet().add(key,value,timestamp);
        Long total=redisTemplate.opsForZSet().size(key);
       if (total!=null && total>MAX_HISTORY){
           redisTemplate.opsForZSet().removeRange(key,0,total-MAX_HISTORY-1);
       }
       updateMetadata(userid,ipAddress,deviceType,timestamp);
        log.info("Recorded login for user:{} ,ipAddress:{} ,deviceType:{}",userid,ipAddress,deviceType);
    }


    public List<Map<String,String>> getLoginHistory(String userId){
        String key=PREFIX+userId;
        Set<Object> entries = redisTemplate.opsForZSet()
                .reverseRange(key, 0, MAX_HISTORY - 1L);
        if (entries==null || entries.isEmpty()) return Collections.emptyList();

        return entries.stream()
                    .filter(Objects::nonNull)
                    .map(entry->{
                String entryStr=entry.toString();
                String[] parts=entryStr.split("\\|");
                        String readableTime;
                        try {
                            long epoch = Long.parseLong(parts[0]);
                            readableTime = FORMATTER.format(Instant.ofEpochSecond(epoch));
                        } catch (Exception e) {
                            readableTime = parts[0]; // fallback to raw timestamp
                        }
                if (parts.length<3) return null;
                Map<String,String> loginInfo=new HashMap<>();
                loginInfo.put("timestamp",readableTime);
                loginInfo.put("ipAddress",parts[1]);
                loginInfo.put("deviceType",parts[2]);
                return loginInfo;
            })
                    .filter(Objects::nonNull).
                    collect(Collectors.toList());





    }

    public void updateMetadata(String userId,String ipAddress,String deviceType,long timestamp){
        String key=METADATA_PREFIX+userId;
        Map<Object,Object> currentdata=redisTemplate.opsForHash().entries(key);

        String lastIp=(String) currentdata.getOrDefault("lastIp","");
        String riskFlag=(String) currentdata.getOrDefault("riskFlag","SAFE");

        if (!lastIp.isEmpty()&& !lastIp.equals(ipAddress))
            riskFlag="SUSPICIOUS";
        redisTemplate.opsForHash().put(key,"lastLogin",FORMATTER.format(Instant.ofEpochSecond(timestamp)));
        redisTemplate.opsForHash().put(key,"sessionCount",String.valueOf(getSessionCount(userId)));
        redisTemplate.opsForHash().put(key,"deviceType",deviceType);
        redisTemplate.opsForHash().put(key,"lastIp",ipAddress);
        redisTemplate.opsForHash().put(key,"riskFlag",riskFlag);

        log.info("User metadata updated for user: {}", userId);

    }
    private int getSessionCount(String userId) {
        String key = SESSION_PREFIX + userId;
        try {
            String userJson = (String) redisTemplate.opsForValue().get(key);
            if (userJson == null) {
                log.warn("No session data found in Redis for user {}", userId);
                return 0;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> userData = objectMapper.readValue(userJson, new TypeReference<>() {});
            List<?> sessions = (List<?>) userData.get("sessions");
            return sessions != null ? sessions.size() : 0;
        } catch (Exception e) {
            log.error("Failed to parse session count for user {}: {}", userId, e.getMessage());
        }
        return 0;
    }




    public Map<String,String> getUserMetadata(String userId){
        String key=METADATA_PREFIX+userId;
        Map<Object,Object> metadata=redisTemplate.opsForHash().entries(key);

        Map<String,String> userMetadata=new HashMap<>();
        userMetadata.put("lastLogin",(String) metadata.getOrDefault("lastLogin",""));
        userMetadata.put("sessionCount",(String) metadata.getOrDefault("sessionCount","0"));
        userMetadata.put("deviceType",(String) metadata.getOrDefault("deviceType",""));
        userMetadata.put("lastIp",(String)metadata.getOrDefault("lastIp",""));
        userMetadata.put("riskFlag",(String) metadata.getOrDefault("riskFlag","SAFE"));

        return userMetadata;
    }




}
