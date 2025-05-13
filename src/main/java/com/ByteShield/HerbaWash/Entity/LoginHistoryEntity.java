package com.ByteShield.HerbaWash.Entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginHistoryEntity {
    private String ipAddress;
    private String deviceType;
    private long timestamp;
}
