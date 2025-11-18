package com.example.petservice.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.petcommon.context.UserContext;
import static com.example.petcommon.result.Result.error;
import com.example.petcommon.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Value("${pet.jwt.user-secret-key:petProjectSecretKey123456}")
    private String secretKey;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        // 从请求头中获取token
        String token = extractToken(request);
        
        if (token != null) {
            try {
                // 解析token获取用户信息
                Claims claims = JwtUtil.parseJWT(secretKey, token);
                
                // 检查token是否已过期
                if (JwtUtil.isTokenExpired(claims)) {
                    log.warn("Token已过期，请求URI: {}", requestUri);
                    return sendErrorResponse(response, 401, "Token已过期");
                }
                
                // 从claims中获取userId和username
                Long userId = com.example.petcommon.utils.JwtUtil.extractUserId(claims);
                String userName = com.example.petcommon.utils.JwtUtil.extractUsername(claims);
                
                // 如果无法获取用户信息，返回401错误
                if (userId == null || userName == null) {
                    log.warn("无法从claims中提取用户信息，返回401错误");
                    return sendErrorResponse(response, 401, "Token中缺少用户信息");
                }
                
                // 设置用户上下文
                UserContext.setUserContext(userId, userName);
            } catch (Exception e) {
                // token解析失败，返回401
                log.warn("Token解析失败，请求URI: {}, 错误: {}", requestUri, e.getMessage());
                return sendErrorResponse(response, 401, "Token无效或已过期");
            }
        } else {
            // 没有token，返回401
            log.warn("请求中没有token，请求URI: {}", requestUri);
            return sendErrorResponse(response, 401, "请先登录");
        }
        
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
    
    /**
     * 发送错误响应
     */
    private boolean sendErrorResponse(HttpServletResponse response, int code, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String json = mapper.writeValueAsString(error(code, message));
            response.getWriter().write(json);
        } catch (Exception ex) {
            try {
                response.getWriter().write(String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, message));
            } catch (Exception e) {
                // 最后的兜底处理，记录日志
                log.error("发送错误响应失败: {}", e.getMessage());
            }
        }
        return false;
    }
}