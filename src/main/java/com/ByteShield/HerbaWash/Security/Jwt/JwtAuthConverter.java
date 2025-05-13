package com.ByteShield.HerbaWash.Security.Jwt;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter=new JwtGrantedAuthoritiesConverter();
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt){
        Collection<GrantedAuthority> authorities= Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractRoles(jwt).stream()
        ).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt,authorities);

    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt){
        Set<String> roles=new HashSet<>();
        Map<String,Object> realmAccess=jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles.addAll((Collection<String>) realmAccess.get("roles"));
            System.out.println("Accessed by realm roles");

        }
        String clientId=jwt.getClaim("azp");
        Map<String,Object> resourseAccess=jwt.getClaim("resource_access");
        if (resourseAccess != null && realmAccess.containsKey(clientId)) {
            Map<String,Object> clientRoles= (Map<String, Object>) resourseAccess.get(clientId);
            if (clientRoles.containsKey("roles")){
                roles.addAll((Collection<String>) clientRoles.get("roles") );
                System.out.println("Accessed by client roles");
            }

        }
        System.out.println("Extracted Roles: "+roles);
        return roles.stream()
                .map(role->new SimpleGrantedAuthority("ROLE_"+role.toUpperCase()))
                .collect(Collectors.toSet());

    }
}
