package com.ByteShield.HerbaWash.Security;

import com.ByteShield.HerbaWash.Services.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class TokenBlacklistFilter extends OncePerRequestFilter {
    private final TokenRevocationService revocationService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token=extractToken(request);
        String userId="userId";
        if (token != null && revocationService.isTokenRevoked(token,userId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is revoked");
        }
        filterChain.doFilter(request,response);
    }
    private String extractToken(HttpServletRequest request){
        String bearer=request.getHeader("Authorization");
        if (bearer!=null && bearer.startsWith("Bearer "))
            return bearer.substring(7);
        return null;
    }
}
