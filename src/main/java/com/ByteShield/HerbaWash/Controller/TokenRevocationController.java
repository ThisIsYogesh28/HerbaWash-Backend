package com.ByteShield.HerbaWash.Controller;

import com.ByteShield.HerbaWash.Services.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TokenRevocationController {
    private final TokenRevocationService revocationService;

    @PostMapping("/revoke-token")
    public String revoke(@RequestParam String token){
        revocationService.revokeToken(token,"user");
        return "Token revoked";
    }
}
