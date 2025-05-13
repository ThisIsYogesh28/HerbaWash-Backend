package com.ByteShield.HerbaWash.Utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;

public class AuthUtils {

    public static String getUserId(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("sub"); // Keycloak stores userId as "sub"
        }
        return null;
    }

    public static String getUsername(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        return null;
    }

    public static List<String> getRoles(Authentication authentication) {
        if (authentication == null) return List.of();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsStringList("realm_access.roles");
        }
        return List.of();
    }
}
