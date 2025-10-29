package com.example.petcommon.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.petcommon.utils.JwtUtil;
import com.example.petcommon.utils.UserContext;

import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * 用户上下文拦截器
 * 用于从JWT令牌中提取用户信息并设置到ThreadLocal中
 */
@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    @Value("${pet.jwt.user-secret-key:petProjectSecretKey123456}")
    private String secretKey;

    // 定义公开接口列表 - 无需token即可访问
    private static final List<String> PUBLIC_URLS = new ArrayList<>();
    
    static {
        // 登录和注册接口 - 精确匹配
        PUBLIC_URLS.add("/api/user/login");
        PUBLIC_URLS.add("/api/user/register");
        
        // 公开的文章接口 - 前缀匹配
        PUBLIC_URLS.add("/api/articles/");
        
        // 公开的宠物详情接口 - 前缀匹配
        PUBLIC_URLS.add("/api/pets/details/");
        
        // 媒体文件下载接口 - 前缀匹配
        PUBLIC_URLS.add("/api/media/download/");
        
        // 图片资源接口 - 前缀匹配
        PUBLIC_URLS.add("/api/images/");
        
        // 媒体文件接口 - 前缀匹配
        PUBLIC_URLS.add("/api/media/");
        
        // 其他可能的公开接口 - 精确匹配
        PUBLIC_URLS.add("/api/health");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        
        log.debug("拦截器处理请求: URI={}", requestUri);
        
        // 打印所有请求头
        log.debug("=== 所有请求头信息 ===");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.debug("请求头: {} = {}", headerName, headerValue);
        }
        log.debug("=== 请求头信息结束 ===");
        
        // 检查是否为公开接口
        boolean isPublicEndpoint = isPublicUrl(requestUri);
        
        // 如果是公开接口，直接设置默认用户上下文并返回
        if (isPublicEndpoint) {
            log.debug("公开接口，使用默认用户上下文: userId=1, username=system");
            UserContext.setUserContext(1L, "system");
            return true;
        }
        
        // 从请求头中获取token - 支持多种header名称
        String token = request.getHeader("Authorization");
        
        // 如果没有Authorization头，尝试authentication头（前端使用的名称）
        if (token == null || token.isEmpty()) {
            token = request.getHeader("authentication");
        }
        
        log.debug("从请求头获取token: Authorization={}, authentication={}", 
                  request.getHeader("Authorization"), request.getHeader("authentication"));
        
        // 添加更多调试信息
        log.debug("请求方法: {}", request.getMethod());
        log.debug("请求参数: {}", request.getQueryString());
        
        if (token != null) {
            // 如果token以Bearer开头，去掉前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            log.debug("解析JWT token: {}", token);
            
            try {
                // 解析token获取用户信息
                Claims claims = JwtUtil.parseJWT(secretKey, token);
                
                log.debug("JWT解析成功，claims: {}", claims);
                
                // 从claims中获取userId和username，使用专用工具类
                Long userId = com.example.petcommon.utils.TokenRefreshUtil.extractUserId(claims);
                String userName = com.example.petcommon.utils.TokenRefreshUtil.extractUsername(claims);
                
                log.debug("从claims提取用户信息: userId={}, username={}", userId, userName);
                
                // 如果仍然无法获取用户信息，使用默认的系统用户
                if (userId == null || userName == null) {
                    log.warn("无法从claims中提取用户信息，使用默认系统用户: userId=1, username=system");
                    userId = 1L;
                    userName = "system";
                }
                
                // 设置用户上下文
                log.debug("设置用户上下文: userId={}, username={}", userId, userName);
                UserContext.setUserContext(userId, userName);
            } catch (Exception e) {
                // token解析失败，返回401
                log.warn("Token解析失败，请求URI: {}, 错误信息: {}", requestUri, e.getMessage());
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
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @NonNull Exception ex) throws Exception {
        // 请求完成后清除ThreadLocal，防止内存泄漏
        log.debug("请求完成，清除用户上下文");
        UserContext.clear();
    }
    
    /**
     * 判断是否为公开接口（无需token即可访问）
     * 修改匹配逻辑：只让特定的接口作为公开接口，而不是所有以某个路径开头的接口
     * 
     * 匹配规则：
     * 1. 以"/"结尾的路径：允许前缀匹配（如媒体文件接口）
     * 2. 不以"/"结尾的路径：要求精确匹配（如登录、注册接口）
     */
    private boolean isPublicUrl(String uri) {
        for (String publicUrl : PUBLIC_URLS) {
            // 对于以"/"结尾的路径，允许前缀匹配（如媒体文件接口）
            if (publicUrl.endsWith("/")) {
                if (uri.startsWith(publicUrl)) {
                    log.debug("前缀匹配到公开接口: {} -> {}", uri, publicUrl);
                    return true;
                }
            } 
            // 对于不以"/"结尾的路径，要求精确匹配（如登录、注册接口）
            else if (uri.equals(publicUrl)) {
                log.debug("精确匹配到公开接口: {} -> {}", uri, publicUrl);
                return true;
            }
        }
        log.debug("非公开接口: {}", uri);
        return false;
    }
}