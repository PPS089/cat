package com.example.petcommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        try {
            // 参数验证
            if (secretKey == null || secretKey.trim().isEmpty()) {
                throw new IllegalArgumentException("密钥不能为空");
            }
            if (ttlMillis <= 0) {
                throw new IllegalArgumentException("过期时间必须大于0");
            }
            if (claims == null) {
                throw new IllegalArgumentException("claims不能为空");
            }
            
            // 生成JWT的时间
            long expMillis = System.currentTimeMillis() + ttlMillis;
            Date exp = new Date(expMillis);

            // 生成安全的密钥
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            // 设置jwt的body
            JwtBuilder builder = Jwts.builder()
                    // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                    .setClaims(claims)
                    // 设置签名使用的签名算法和签名使用的秘钥
                    .signWith(key)
                    // 设置过期时间
                    .setExpiration(exp);

            String token = builder.compact();
            log.debug("JWT token生成成功，过期时间: {}", exp);
            return token;
            
        } catch (Exception e) {
            log.error("JWT token生成失败，错误: {}", e.getMessage(), e);
            throw new RuntimeException("JWT token生成失败: " + e.getMessage());
        }
    }

    /**
     * 解析JWT
     *
     * @param secretKey jwt秘钥
     * @param token     待解析的token
     * @return Claims对象
     */
    public static Claims parseJWT(String secretKey, String token) {
        try {
            // 参数验证
            if (secretKey == null || secretKey.trim().isEmpty()) {
                throw new IllegalArgumentException("密钥不能为空");
            }
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("token不能为空");
            }
            
            // 生成安全的密钥
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            
            // 解析JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token).getBody();
            
            log.debug("JWT token解析成功，用户: {}", claims.get("username"));
            return claims;
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 处理过期token - 仍然返回claims
            log.debug("JWT token已过期，但仍然返回claims");
            return e.getClaims();
        } catch (Exception e) {
            log.error("JWT token解析失败，错误: {}", e.getMessage());
            throw new RuntimeException("JWT token解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查token是否已过期
     *
     * @param claims JWT claims对象
     * @return true表示已过期，false表示未过期
     */
    public static boolean isTokenExpired(Claims claims) {
        if (claims == null) {
            return true;
        }
        
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return true;
        }
        
        return expiration.before(new Date());
    }

    /**
     * 从对象中提取Long值
     * @param obj 可能包含Long值的对象
     * @return 提取的Long值，如果无法提取则返回null
     */
    private static Long extractLongValue(Object obj) {
        if (obj instanceof Long num) {
            return num;
        }

        if (obj instanceof Integer num) {
            return num.longValue();
        }

        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    /**
     * 从Claims中安全地获取userId
     * @param claims JWT claims对象
     * @return 用户ID，如果获取失败返回null
     */
    public static Long extractUserId(Claims claims) {
        if (claims == null) {
            log.warn("Claims为null，无法提取userId");
            return null;
        }

        try {
            return extractLongValue(claims.get("userId"));
        } catch (ClassCastException | NullPointerException e) {
            log.debug("提取userId失败: {}", e.getMessage());
        }

        log.warn("无法从claims中提取userId，返回null");
        return null;
    }

    /**
     * 从Claims中提取用户名
     * @param claims JWT claims
     * @return 用户名，如果获取失败返回null
     */
    public static String extractUsername(Claims claims) {
        if (claims == null) {
            return null;
        }
        Object usernameObj = claims.get("username");
        if (usernameObj instanceof String) {
            String username = (String) usernameObj;
            return username.isEmpty() ? null : username;
        }
        return null;
    }
}