package com.coopfinance.system.config;

import com.coopfinance.system.common.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/register")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (!jwtUtil.isTokenExpired(token)) {
                    Claims claims = jwtUtil.extractClaims(token);
                    String role = claims.get("role", String.class);
                    // 简单的按角色前缀拦截
                    if (uri.startsWith("/api/admin") && !"admin".equals(role)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                    if (uri.startsWith("/api/finance") && !"finance".equals(role)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                    if (uri.startsWith("/api/user") && !"user".equals(role)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                    
                    request.setAttribute("username", claims.get("username"));
                    request.setAttribute("role", role);
                    request.setAttribute("name", claims.get("name"));
                    return true;
                }
            } catch (Exception e) {
                // validation failed
            }
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
