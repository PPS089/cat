package com.example.petservice.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文拦截器
 * 用于从JWT令牌中提取用户信息并设置到ThreadLocal中
 */
@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    @Value("${pet.jwt.user-secret-key:petProjectSecretKey123456}")
    private String secretKey;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        
        log.debug("处理请求: {}", requestUri);
        
        // 从请求头中获取token
        String token = extractToken(request);
        
        if (token != null) {
            try {
                // 解析token获取用户信息
                Claims claims = JwtUtil.parseJWT(secretKey, token);
                
                // 从claims中获取userId和username
                Long userId = com.example.petcommon.utils.TokenRefreshUtil.extractUserId(claims);
                String userName = com.example.petcommon.utils.TokenRefreshUtil.extractUsername(claims);
                
                // 如果无法获取用户信息，返回401错误
                if (userId == null || userName == null) {
                    log.warn("无法从claims中提取用户信息，返回401错误");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"Token中缺少用户信息\"}");
                    return false;
                }
                
                // 设置用户上下文
                UserContext.setUserContext(userId, userName);
            } catch (Exception e) {
                // token解析失败，返回401
                log.warn("Token解析失败，请求URI: {}, 错误: {}", requestUri, e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
                return false;
            }
        } else {
            // 没有token，返回401
            log.warn("请求中没有token，请求URI: {}", requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @NonNull Exception ex) throws Exception {
        // 请求完成后清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
    
    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        
        // 如果没有Authorization头，尝试authentication头
        if (token == null || token.isEmpty()) {
            token = request.getHeader("authentication");
        }
        
        // 如果token以Bearer开头，去掉前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return token;
    }
}