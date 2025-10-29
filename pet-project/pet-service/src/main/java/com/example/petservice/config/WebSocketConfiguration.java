package com.example.petservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // 新增注解
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration // 必须添加，否则 Spring 不会加载该配置类
public class WebSocketConfiguration {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}