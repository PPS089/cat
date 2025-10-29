package com.example.petcommon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pet.jwt")
public class JwtProperties {
    /**
     * 用户jwt秘钥
     */
    private String userSecretKey;
    
    /**
     * 用户jwt过期时间
     */
    private long userTtl;
    
    /**
     * 管理端jwt秘钥
     */
    private String adminSecretKey;
    
    /**
     * 管理端jwt过期时间
     */
    private long adminTtl;
}