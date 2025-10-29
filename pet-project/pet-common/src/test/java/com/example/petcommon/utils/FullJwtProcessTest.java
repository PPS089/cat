package com.example.petcommon.utils;

import com.example.petcommon.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FullJwtProcessTest {

    @Test
    public void testFullJwtProcess() {
        // 模拟JwtProperties配置
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setUserSecretKey("itheimaSecretKeyThatIsLongEnoughForHS256AlgorithmRequirements");
        jwtProperties.setUserTtl(86400000); // 24小时
        
        // 模拟用户登录成功，用户ID为214
        Long userId = 214L;
        String username = "testuser";
        
        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        System.out.println("生成的token: " + token);
        
        // 模拟拦截器解析token
        try {
            Claims parsedClaims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            System.out.println("解析的claims: " + parsedClaims);
            
            // 检查userId的实际类型
            Object userIdObj = parsedClaims.get("userId");
            System.out.println("userId类型: " + userIdObj.getClass().getName());
            System.out.println("userId值: " + userIdObj);
            
            // 使用TokenRefreshUtil提取userId
            Long extractedUserId = TokenRefreshUtil.extractUserId(parsedClaims);
            String extractedUsername = TokenRefreshUtil.extractUsername(parsedClaims);
            
            System.out.println("提取的userId: " + extractedUserId);
            System.out.println("提取的username: " + extractedUsername);
            
            // 验证提取的用户信息
            assertNotNull(extractedUserId, "提取的userId不应为null");
            assertEquals(userId, extractedUserId, "userId应匹配");
            assertEquals(username, extractedUsername, "username应匹配");
            
            // 模拟设置用户上下文
            UserContext.setUserContext(extractedUserId, extractedUsername);
            
            // 验证UserContext中的用户信息
            assertEquals(extractedUserId, UserContext.getCurrentUserId(), "UserContext中的userId应匹配");
            assertEquals(extractedUsername, UserContext.getCurrentUserName(), "UserContext中的username应匹配");
            
        } catch (Exception e) {
            fail("JWT处理过程中出现异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testUserContextInterceptorLogic() {
        // 模拟JwtProperties配置
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setUserSecretKey("itheimaSecretKeyThatIsLongEnoughForHS256AlgorithmRequirements");
        jwtProperties.setUserTtl(86400000); // 24小时
        
        // 模拟用户登录成功，用户ID为214
        Long userId = 214L;
        String username = "testuser";
        
        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        
        // 模拟拦截器中的逻辑
        try {
            // 解析token获取用户信息
            Claims parsedClaims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            
            // 从claims中获取userId和username，使用专用工具类
            Long extractedUserId = TokenRefreshUtil.extractUserId(parsedClaims);
            String extractedUsername = TokenRefreshUtil.extractUsername(parsedClaims);
            
            System.out.println("拦截器逻辑 - 提取的userId: " + extractedUserId);
            System.out.println("拦截器逻辑 - 提取的username: " + extractedUsername);
            
            // 如果仍然无法获取用户信息，使用默认的系统用户
            if (extractedUserId == null || extractedUsername == null) {
                extractedUserId = 1L;
                extractedUsername = "system";
            }
            
            // 设置用户上下文
            UserContext.setUserContext(extractedUserId, extractedUsername);
            
            // 验证UserContext中的用户信息
            assertEquals(extractedUserId, UserContext.getCurrentUserId(), "UserContext中的userId应匹配");
            assertEquals(extractedUsername, UserContext.getCurrentUserName(), "UserContext中的username应匹配");
            
            // 模拟请求完成后的清理
            UserContext.clear();
            
        } catch (Exception e) {
            fail("拦截器逻辑处理过程中出现异常: " + e.getMessage());
        }
    }
}