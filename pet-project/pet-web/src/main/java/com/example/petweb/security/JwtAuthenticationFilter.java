package com.example.petweb.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.properties.JwtProperties;
import com.example.petcommon.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * @author 33185
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws IOException {
        boolean proceed = true;
        try {
            String authHeader = request.getHeader("Authorization");
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = authHeader.substring(7);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            if (JwtUtil.isTokenExpired(claims)) {
                writeError(response, ErrorCode.TOKEN_INVALID_OR_EXPIRED);
                proceed = false;
                return;
            }
            Long userId = JwtUtil.extractUserId(claims);
            String username = JwtUtil.extractUsername(claims);
            String rawRole = JwtUtil.extractRole(claims);
            String role = normalizeRole(rawRole);
            Integer adminShelterId = JwtUtil.extractAdminShelterId(claims);
            
            // 【增强JWT验证】详细的字段验证和日志记录
            if (userId == null) {
                log.warn("JWT缺少userId: token={}", token.substring(0, Math.min(20, token.length())));
                writeError(response, ErrorCode.TOKEN_INVALID_OR_EXPIRED);
                proceed = false;
                return;
            }
            
            if (!StringUtils.hasText(username)) {
                log.warn("JWT缺少username: token={}", token.substring(0, Math.min(20, token.length())));
                writeError(response, ErrorCode.TOKEN_INVALID_OR_EXPIRED);
                proceed = false;
                return;
            }
            
            if (role == null) {
                log.warn("JWT包含无效的role: rawRole={}, userId={}, username={}", rawRole, userId, username);
                writeError(response, ErrorCode.FORBIDDEN, "Token中包含无效的角色信息");
                proceed = false;
                return;
            }
            
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserContext.setUserContext(userId, username, role, adminShelterId);
            log.debug("JWT认证成功: userId={}, username={}, role={}, adminShelterId={}", userId, username, role, adminShelterId);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.warn("JWT认证失败: {}", ex.getMessage());
            writeError(response, ErrorCode.UNAUTHORIZED);
            proceed = false;
        } finally {
            UserContext.clear();
            if (!proceed) {
                SecurityContextHolder.clearContext();
            }
        }
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        String upper = role.trim().toUpperCase();
        if (ROLE_ADMIN.equals(upper) || ROLE_USER.equals(upper)) {
            return upper;
        }
        // 【增强验证】返回null时记录警告
        log.warn("无效的role值: {}", role);
        return null;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":" + errorCode.getCode() + ",\"msg\":\"" + errorCode.getMessage() + "\"}");
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode, String customMessage) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":" + errorCode.getCode() + ",\"msg\":\"" + customMessage + "\"}");
    }
}
