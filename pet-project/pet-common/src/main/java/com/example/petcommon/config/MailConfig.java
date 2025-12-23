package com.example.petcommon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 * 配置JavaMailSender bean用于发送邮件
 */
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfig {
    
    private String host;
    private int port;
    private String username;
    private String password;
    private Properties properties = new Properties();
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setJavaMailProperties(properties != null ? properties : new Properties());
        return mailSender;
    }
    
    // Getters and Setters
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties != null ? properties : new Properties();
    }
}
