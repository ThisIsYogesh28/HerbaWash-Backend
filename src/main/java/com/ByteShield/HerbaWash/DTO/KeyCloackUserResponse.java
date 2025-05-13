package com.ByteShield.HerbaWash.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeyCloackUserResponse {
    private String userId;
    private String username;
    private String email;
    private Map<String, List<String>> clientRoles;
}
