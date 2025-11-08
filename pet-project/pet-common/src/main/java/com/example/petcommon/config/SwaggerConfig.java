package com.example.petcommon.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI petProjectOpenAPI() {
        // 定义安全方案
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
        
        // 配置服务器地址列表
        List<Server> servers = new ArrayList<>();
        servers.add(new Server()
                .url("http://localhost:8080/api")
                .description("开发环境"));
        servers.add(new Server()
                .url("http://192.168.1.100:8080/api")
                .description("测试环境"));
        servers.add(new Server()
                .url("https://api.yourdomain.com/api")
                .description("生产环境"));

        return new OpenAPI()
                .info(new Info()
                        .title("宠物管理系统 API 文档")
                        .description("基于 Spring Boot + MyBatis-Plus 的宠物管理系统接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .servers(servers)
                .components(new Components()
                        // 添加安全方案
                        .addSecuritySchemes("bearer-key", securityScheme)
                        // 添加通用Result Schema定义
                        .addSchemas("Result", new Schema<>()
                                .type("object")
                                .addProperty("code", new Schema<Integer>()
                                        .type("integer")
                                        .example(200))
                                .addProperty("msg", new Schema<String>()
                                        .type("string")
                                        .example("success"))
                                .addProperty("data", new Schema<>()
                                        .type("object")
                                        .nullable(true))
                        )
                        // 添加ResultMapStringString Schema定义（用于400错误响应）
                        .addSchemas("ResultMapStringString", new Schema<>()
                                .type("object")
                                .addProperty("code", new Schema<Integer>()
                                        .type("integer")
                                        .example(400))
                                .addProperty("msg", new Schema<String>()
                                        .type("string")
                                        .example("请求参数错误"))
                                .addProperty("data", new MapSchema()
                                        .additionalProperties(new Schema<String>()
                                                .type("string"))
                                        .example("{\"username\": \"用户名不能为空\", \"password\": \"密码不能为空\"}"))
                        )
                )
                // 添加全局安全要求
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
    }
}