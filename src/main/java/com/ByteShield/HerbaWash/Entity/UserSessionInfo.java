package com.ByteShield.HerbaWash.Entity;

import jakarta.persistence.Id;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserSessionInfo {

    private String sessionId;
    private String ipAddress;
    private long createdAt;
    private long lastAccess;
    private String status;


}
