package com.example.petservice.service.impl;

import com.example.petcommon.config.JwtProperties;
import com.example.petcommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthenticationTest {

    private final JwtProperties jwtProperties = new JwtProperties();

    public JwtAuthenticationTest() {
        jwtProperties.setUserSecretKey("itheimaSecretKeyThatIsLongEnoughForHS256AlgorithmRequirements");
        jwtProperties.setUserTtl(86400000); // 24小时
    }

    @Test
    public void testCreateAndParseJWT() {
        // 准备claims数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("username", "testuser");

        // 生成token
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        assertNotNull(token, "生成的token不应为null");
        assertFalse(token.isEmpty(), "生成的token不应为空");

        // 解析token
        Claims parsedClaims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        assertNotNull(parsedClaims, "解析的claims不应为null");
        assertEquals(1L, ((Number) parsedClaims.get("userId")).longValue(), "userId应匹配");
        assertEquals("testuser", parsedClaims.get("username"), "username应匹配");
    }

    @Test
    public void testTokenExpiration() throws InterruptedException {
        // 创建一个很快过期的token（100毫秒）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("username", "testuser");

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), 100, claims);
        assertNotNull(token, "生成的token不应为null");

        // 等待token过期
        Thread.sleep(150);

        // 尝试解析过期的token，应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        }, "过期的token应该抛出异常");
    }

    @Test
    public void testLooseParseExpiredToken() throws InterruptedException {
        // 创建一个很快过期的token（100毫秒）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("username", "testuser");

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), 100, claims);
        assertNotNull(token, "生成的token不应为null");

        // 等待token过期
        Thread.sleep(150);

        // 使用宽松模式解析过期的token，应该能获取到claims
        Claims parsedClaims = JwtUtil.parseJWTLoose(jwtProperties.getUserSecretKey(), token);
        assertNotNull(parsedClaims, "宽松模式下应能解析过期的token");
        assertEquals(1L, ((Number) parsedClaims.get("userId")).longValue(), "userId应匹配");
        assertEquals("testuser", parsedClaims.get("username"), "username应匹配");
    }
}