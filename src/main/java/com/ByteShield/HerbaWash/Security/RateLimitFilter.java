package com.ByteShield.HerbaWash.Security;

import com.ByteShield.HerbaWash.Services.DeviceDetectorService;
import com.ByteShield.HerbaWash.Services.LoginHistoryService;
import com.ByteShield.HerbaWash.Services.RedisRateLimiterService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {
    private final RedisRateLimiterService rateLimiterService;
    @Autowired
    private LoginHistoryService loginHistoryService;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        if (!rateLimiterService.isAllowed(httpReq)) {
            log.warn("Rate limit exceeded for user/ip", httpReq.getRemoteAddr());
            httpRes.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            String responseJson = "{\"error\":\"Too many requests.Please slow down.\"}";
            httpRes.getWriter().write(responseJson);
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {

                String userId = com.ByteShield.HerbaWash.Utils.AuthUtils.getUserId(authentication);
                if (userId != null) {
                    String ipAddress = request.getRemoteAddr();
                    String userAgent = ((HttpServletRequest) request).getHeader("User-Agent");
                    String deviceType = DeviceDetectorService.detectDeviceType(userAgent);
                    loginHistoryService.recordLogin(userId, ipAddress, deviceType);
                }

        }
            chain.doFilter(request, response);
        }

    }


