package com.ByteShield.HerbaWash.Controller;

import com.ByteShield.HerbaWash.Services.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class UserSyncController {
    private final UserSyncService userSyncService;
    @GetMapping("/sync-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerUserSync(){
        log.info("Manual sync triggered via REST Api");
        userSyncService.syncUser("MANUAL");
        return ResponseEntity.ok("Manual keycloak user sync completed successfully");
    }
    @GetMapping("/ok")
    public ResponseEntity<String> ok(){
        return ResponseEntity.ok("ok");
    }
}
