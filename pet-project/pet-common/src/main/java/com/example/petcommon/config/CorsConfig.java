package com.example.petcommon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


/**
 * 跨域配置类
 * 解决前端访问后端API时的跨域问题
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * 允许所有来源、所有请求头、所有HTTP方法
     * 并允许携带认证信息（如JWT token）
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // 允许所有来源访问
        corsConfiguration.addAllowedOriginPattern("*");
        
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        
        // 允许所有HTTP方法（GET, POST, PUT, DELETE, OPTIONS等）
        corsConfiguration.addAllowedMethod("*");
        
        // 允许携带认证信息（如cookies、JWT token等）
        corsConfiguration.setAllowCredentials(true);
        
        // 预检请求的有效期，单位秒（24小时）
        corsConfiguration.setMaxAge(86400L);
        
        // 对所有的API路径生效
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}