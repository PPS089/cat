package com.example.petweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * 全局 OpenAPI/Knife4j 配置：
 * - 定义 Authorization 头的 Bearer 认证
 * - 作为全局安全要求，方便在 UI 中一处填写后调用所有接口
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI petOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pet Project API")
                        .description("管理员与用户共用的接口文档")
                        .version("v1.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private SecurityScheme bearerScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);
    }
}
