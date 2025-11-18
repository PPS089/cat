package com.example.petcommon.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

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
     * 刷新token秘钥
     */
    private String refreshSecretKey;

    /**
     * 刷新token过期时间
     */
    private long refreshTtl;
}