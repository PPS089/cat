package com.example.petcommon.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    public void testCreateAndParseJWTWithLongUserId() {
        // 准备claims数据，使用Long类型的userId
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 214L); // 使用Long类型
        claims.put("username", "testuser");

        // 生成token
        String token = JwtUtil.createJWT("testSecretKey12345678901234567890", 86400000, claims);
        assertNotNull(token, "生成的token不应为null");
        assertFalse(token.isEmpty(), "生成的token不应为空");

        // 解析token
        Claims parsedClaims = JwtUtil.parseJWT("testSecretKey12345678901234567890", token);
        assertNotNull(parsedClaims, "解析的claims不应为null");
        
        // 验证userId类型和值
        Object userIdObj = parsedClaims.get("userId");
        assertNotNull(userIdObj, "userId不应为null");
        System.out.println("userId类型: " + userIdObj.getClass().getName());
        System.out.println("userId值: " + userIdObj);
        
        // 验证username
        assertEquals("testuser", parsedClaims.get("username"), "username应匹配");
    }
    
    @Test
    public void testTokenRefreshUtilExtractUserIdWithInteger() {
        // 准备claims数据，使用Integer类型的userId（模拟JWT库的行为）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 214); // 使用Integer类型，模拟JWT库的行为
        claims.put("username", "testuser");

        // 生成token
        String token = JwtUtil.createJWT("testSecretKey12345678901234567890", 86400000, claims);
        
        // 解析token
        Claims parsedClaims = JwtUtil.parseJWT("testSecretKey12345678901234567890", token);
        
        // 使用TokenRefreshUtil提取userId
        Long userId = TokenRefreshUtil.extractUserId(parsedClaims);
        assertNotNull(userId, "提取的userId不应为null");
        assertEquals(214L, userId, "userId应为214");
    }
}