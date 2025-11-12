package com.example.petservice.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.petservice.interceptor.UserContextInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    @NonNull
    private UserContextInterceptor userContextInterceptor;

    @Value("${app.upload-dir:./uploads/media}")
    private String uploadDir;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 注册用户上下文拦截器（优先级最高，先设置用户上下文）
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 用户相关接口
                        "/user/login",
                        "/user/register",
                        "/user",
                        
                        // 公开的文章接口
                        "/articles/**",
                        
                        // 公开的宠物详情接口
                        "/pets/details/**",
                        
                        // 媒体文件下载接口（只排除下载，不包括上传）
                        "/media/download/**",
                        
                        // 静态资源接口
                        "/images/**", 
                        "/static/**",
                        
                        // Swagger相关接口
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        
                        // Knife4j相关接口
                        "/doc.html",
                        "/webjars/**",
                        
                        // 健康检查接口
                        "/health"
                );
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 添加图片资源映射，支持上传的图片访问
        
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        
        // 添加媒体文件访问映射（仅下载）
        // 将上传目录映射到API接口，用户可以直接访问上传的图片和视频
        registry.addResourceHandler("/media/download/**").addResourceLocations("file:" + uploadDir + "/");
        
        // 添加classpath静态资源映射
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        
        // 添加Knife4j静态资源映射
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
    }
}