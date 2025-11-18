package com.example.petcommon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class Ip2RegionConfig {
    private static final Logger log = LoggerFactory.getLogger(Ip2RegionConfig.class);
    
    @PostConstruct
    public void initIp2Region() {
        try {
            // 设置ip2region文件路径
            String userDir = System.getProperty("user.dir");
            String ip2regionPath = userDir + "/pet-common/src/main/resources/ip2region/ip2region_v4.xdb";
            
            // 检查文件是否存在
            File dbFile = new File(ip2regionPath);
            log.info("检查数据库文件路径: {}", ip2regionPath);
            log.info("文件是否存在: {}", dbFile.exists());
            if (dbFile.exists()) {
                log.info("找到ip2region数据库文件，路径: {}, 大小: {} 字节", ip2regionPath, dbFile.length());
                System.setProperty("ip2region.file", ip2regionPath);
            } else {
                log.warn("未找到ip2region数据库文件: {}", ip2regionPath);
                // 尝试使用相对路径
                String relativePath = "src/main/resources/ip2region/ip2region_v4.xdb";
                File relativeFile = new File(relativePath);
                log.info("检查相对路径: {}", relativePath);
                log.info("相对路径文件是否存在: {}", relativeFile.exists());
                if (relativeFile.exists()) {
                    log.info("找到ip2region数据库文件（相对路径），路径: {}, 大小: {} 字节", relativePath, relativeFile.length());
                    System.setProperty("ip2region.file", relativePath);
                } else {
                    // 尝试使用classpath路径
                    String classpathPath = "pet-common/src/main/resources/ip2region/ip2region_v4.xdb";
                    File classpathFile = new File(classpathPath);
                    log.info("检查classpath路径: {}", classpathPath);
                    log.info("classpath路径文件是否存在: {}", classpathFile.exists());
                    if (classpathFile.exists()) {
                        log.info("找到ip2region数据库文件（classpath路径），路径: {}, 大小: {} 字节", classpathPath, classpathFile.length());
                        System.setProperty("ip2region.file", classpathPath);
                    } else {
                        log.error("无法找到ip2region数据库文件，请确保文件存在于以下路径之一：{} 或 {} 或 {}", ip2regionPath, relativePath, classpathPath);
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化ip2region配置失败: {}", e.getMessage(), e);
        }
    }
}