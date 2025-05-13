package com.ByteShield.HerbaWash.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class SessionAnalyticsDTO {
    private  String userId;
    private int totalSession;
    private int activeSession;
    private int expiredSession;
    private Date lastActiveTime;
}
