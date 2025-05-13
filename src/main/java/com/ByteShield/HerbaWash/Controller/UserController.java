package com.ByteShield.HerbaWash.Controller;

import com.ByteShield.HerbaWash.Services.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j

public class UserController {
    private final LoginHistoryService loginHistoryService;
    @GetMapping("/{userId}/metadata")
    public ResponseEntity<Map<String,String>> getUserMetadata(@PathVariable String userId){
        try {
            Map<String ,String>metadata=loginHistoryService.getUserMetadata(userId);
            return ResponseEntity.ok(metadata);
        }catch (Exception e){
            log.error("Error retrieving user metadata {} :{}",userId,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }

}
