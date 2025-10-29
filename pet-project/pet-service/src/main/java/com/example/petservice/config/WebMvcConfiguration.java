package com.example.petservice.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.petcommon.context.UserContextInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Value("${app.upload-dir:./uploads/media}")
    private String uploadDir;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 注册用户上下文拦截器（优先级最高，先设置用户上下文）
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/images/**", "/static/**", "/media/**");

        // JWT authentication is handled by UserContextInterceptor
        // Add specific interceptors here if needed for admin/user role validation
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加图片资源映射，支持上传的图片访问
        // 映射到Spring Boot的静态资源目录static/images/
        // 由于配置了context-path: /api，实际访问路径为/api/images/
        registry.addResourceHandler("/api/images/**").addResourceLocations("classpath:/static/images/");
        
        // 添加媒体文件访问映射
        // 将上传目录映射到API接口，用户可以直接访问上传的图片和视频
        registry.addResourceHandler("/media/**").addResourceLocations("file:" + uploadDir + "/");
        
        // 添加classpath静态资源映射
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    /**
     * 扩展Spring MVC框架的消息转换器
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 使用Spring容器中的ObjectMapper而不是自定义的JacksonObjectMapper
        // 这样可以确保SpringDoc使用正确的ObjectMapper配置
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                // Spring会自动注入配置好的ObjectMapper
                log.info("使用Spring配置的ObjectMapper");
                return;
            }
        }
        // 如果没有找到MappingJackson2HttpMessageConverter，创建一个新的
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // Spring会自动注入配置好的ObjectMapper，不需要手动设置
        converters.add(0, converter);
    }

    /**
     * 扩展消息转换器配置
     * 用于自定义JSON序列化和反序列化
     */
}