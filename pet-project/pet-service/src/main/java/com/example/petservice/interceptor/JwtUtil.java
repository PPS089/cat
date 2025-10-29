package com.example.petservice.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtil {
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
            
            // 使用新的 API
            Claims claims = Jwts.parserBuilder()
                    // 设置签名的秘钥
                    .setSigningKey(key)
                    // 构建解析器
                    .build()
                    // 设置需要解析的jwt
                    .parseClaimsJws(token).getBody();
            
            log.debug("JWT token解析成功，用户: {}", claims.get("username"));
            return claims;
            
        } catch (Exception e) {
            log.error("JWT token解析失败，错误: {}", e.getMessage());
            throw new RuntimeException("JWT token解析失败: " + e.getMessage());
        }
    }


    /**
     * 验证JWT
     *
     * @param token     待验证的JWT
     * @param secretKey 用于验证的密钥
     * @return 如果验证成功，不返回值；如果验证失败，抛出RuntimeException
     */
    public static Claims verify(String token, String secretKey) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token不能为空");
            }
            if (secretKey == null || secretKey.trim().isEmpty()) {
                throw new RuntimeException("密钥不能为空");
            }
            parseJWT(secretKey, token);
        } catch (RuntimeException e) {
            throw e; // 重新抛出运行时异常
        } catch (Exception e) {
            throw new RuntimeException("Token验证失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 解析JWT（宽松模式）- 即使过期也能读取claims
     * 用于token刷新场景
     *
     * @param secretKey jwt秘钥
     * @param token     待解析的token
     * @return Claims对象
     */
    public static Claims parseJWTLoose(String secretKey, String token) {
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
            
            // 宽松解析 - 忽略过期验证
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token).getBody();
            
            log.debug("JWT token宽松解析成功，用户: {}", claims.get("username"));
            return claims;
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 处理过期token - 宽松模式下仍然返回claims
            log.debug("JWT token已过期，但宽松解析仍然返回claims");
            return e.getClaims();
        } catch (Exception e) {
            log.error("JWT token宽松解析失败，错误: {}", e.getMessage());
            throw new RuntimeException("JWT token宽松解析失败: " + e.getMessage());
        }
    }
}
