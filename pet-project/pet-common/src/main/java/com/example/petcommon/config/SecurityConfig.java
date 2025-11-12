package com.example.petcommon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 配置BCrypt加密器，工作因子设为12
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护，因为使用JWT认证
            .csrf(csrf -> csrf.disable())
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 允许注册和登录接口无需认证
                .requestMatchers("/user", "/user/login", "/admin/employee/login").permitAll()
                // 放行Swagger和Knife4j相关路径 (注意：由于应用配置了context-path=/api，这里不需要再加/api前缀)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/doc.html",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()
                // 其他所有请求都需要认证
                .anyRequest().permitAll() // 暂时允许所有其他请求，因为JWT拦截器会处理认证
            )
            // 配置会话管理为无状态，因为使用JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 禁用默认的HTTP Basic认证
            .httpBasic(httpBasic -> httpBasic.disable())
            // 禁用默认的表单登录
            .formLogin(formLogin -> formLogin.disable());
            
        return http.build();
    }
}