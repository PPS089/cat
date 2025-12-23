package com.example.petcommon.config;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class Ip2RegionConfig {
    private static final Logger log = LoggerFactory.getLogger(Ip2RegionConfig.class);
    
    @PostConstruct
    public void initIp2Region() {
        try {
            String resourcePath = "ip2region/ip2region_v4.xdb";
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            
            if (is != null) {
                is.close();
                log.info("ip2region数据库文件已加载: {}", resourcePath);
            } else {
                log.warn("未找到ip2region数据库文件，IP定位功能将不可用");
            }
        } catch (Exception e) {
            log.error("初始化ip2region配置失败: {}", e.getMessage());
        }
    }
}