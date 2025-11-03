package com.example.petservice.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.petcommon.config.JwtProperties;

/**
 * 测试配置类
 * 提供测试所需的Bean
 */
@Configuration
public class TestConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setUserSecretKey("testSecretKeyForJwtTokenGeneration");
        jwtProperties.setUserTtl(3600L);
        jwtProperties.setAdminSecretKey("testAdminSecretKeyForJwtTokenGeneration");
        jwtProperties.setAdminTtl(7200L);
        return jwtProperties;
    }
}