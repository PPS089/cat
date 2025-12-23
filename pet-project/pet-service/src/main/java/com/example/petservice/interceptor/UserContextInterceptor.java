package com.example.petservice.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.properties.JwtProperties;
import com.example.petcommon.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



/**
 * 用户上下文拦截器
 * 用于从JWT令牌中提取用户信息并设置到ThreadLocal中
 * @author 33185
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {
    
    private final JwtProperties jwtProperties;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String HEADER_SESSION_ROLE = "X-Session-Role";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 从请求头中获取token
        String token = extractToken(request);
        
        if (token == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        Claims claims;
        try {
            claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        } catch (Exception e) {
            throw new BizException(ErrorCode.TOKEN_INVALID_OR_EXPIRED, "Token无效或已过期");
        }

        if (JwtUtil.isTokenExpired(claims)) {
            throw new BizException(ErrorCode.TOKEN_INVALID_OR_EXPIRED, "Token已过期");
        }

        Long userId = JwtUtil.extractUserId(claims);
        String userName = JwtUtil.extractUsername(claims);
        String role = normalizeRole(JwtUtil.extractRole(claims));
        Integer adminShelterId = JwtUtil.extractAdminShelterId(claims);
        if (userId == null || userName == null) {
            throw new BizException(ErrorCode.TOKEN_INVALID_OR_EXPIRED, "Token中缺少用户信息");
        }

        if (role == null) {
            throw new BizException(ErrorCode.FORBIDDEN, "Token缺少角色信息");
        }

        String headerRole = normalizeRole(request.getHeader(HEADER_SESSION_ROLE));
        if (headerRole != null && !role.equals(headerRole)) {
            throw new BizException(ErrorCode.FORBIDDEN, "当前角色与会话不一致，请重新登录");
        }

        // /admin/** 路径必须为管理员
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin") && !ROLE_ADMIN.equals(role)) {
            throw new BizException(ErrorCode.FORBIDDEN, "需要管理员权限");
        }

        UserContext.setUserContext(userId, userName, role, adminShelterId);
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        // 请求完成后清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
    
    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        
        // 如果token以Bearer开头，去掉前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return token;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String upper = role.trim().toUpperCase();
        if (ROLE_ADMIN.equals(upper) || ROLE_USER.equals(upper)) {
            return upper;
        }
        return null;
    }
}
