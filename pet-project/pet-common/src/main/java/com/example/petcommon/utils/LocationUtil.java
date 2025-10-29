package com.example.petcommon.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 地理位置工具类
 * 基于IP地址前缀进行简单的地理位置识别
 */
@Slf4j
public class LocationUtil {

    /**
     * 从IP地址识别地理位置
     * 注：这是一个简化版本，只识别常见的IP段
     * 生产环境建议使用专业的IP地址库（如MaxMind GeoIP、淘宝IP库等）
     * @param ipAddress IP地址
     * @return 地理位置
     */
    public static String getLocationFromIP(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || ipAddress.equals("0.0.0.0")) {
            log.debug("IP地址为null或0.0.0.0，位置为未知");
            return "Unknown Location";
        }
        
        // 检查是否是本地IP地址
        if (isLocalIP(ipAddress)) {
            log.debug("IP {} 是本地IP", ipAddress);
            return "Local Network";
        }
        
        log.debug("开始解析IP {} 的位置", ipAddress);
        
        // 解析IP的第一段（简单的识别逻辑）
        String[] parts = ipAddress.split("\\.");
        if (parts.length < 1) {
            log.debug("IP段数不足，位置为未知");
            return "Unknown Location";
        }
        
        try {
            int firstSegment = Integer.parseInt(parts[0]);
            log.debug("IP的第一段: {}", firstSegment);
            
            // 简单的地理位置映射（仅作示例）
            // 这些是一些常见的IP段范围
            String location;
            if (firstSegment >= 1 && firstSegment <= 9) {
                location = "USA";
            } else if (firstSegment >= 11 && firstSegment <= 42) {
                location = "Europe";
            } else if (firstSegment >= 49 && firstSegment <= 60) {
                location = "Asia";
            } else if (firstSegment >= 101 && firstSegment <= 125) {
                location = "China";
            } else if (firstSegment >= 126 && firstSegment <= 189) {
                location = "Asia-Pacific";
            } else if (firstSegment >= 190 && firstSegment <= 223) {
                location = "South America / Oceania";
            } else {
                location = "Other Location";
            }
            
            log.debug("IP {} 对应的位置: {}", ipAddress, location);
            return location;
        } catch (NumberFormatException e) {
            log.warn("无法解析IP第一段: {}", parts[0], e);
            return "Unknown Location";
        }
    }

    /**
     * 判断是否为本地IP地址
     * @param ipAddress IP地址
     * @return true 如果是本地IP，false 否则
     */
    private static boolean isLocalIP(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }
        
        boolean isLocal = ipAddress.startsWith("127.") ||                    // 本地127.0.0.1
               ipAddress.startsWith("192.168.") ||               // 私网192.168.x.x
               ipAddress.startsWith("10.") ||                    // 私网10.x.x.x
               ipAddress.startsWith("172.") ||                   // 私网172.16.x.x-172.31.x.x
               ipAddress.equals("localhost") ||
               ipAddress.equals("0:0:0:0:0:0:0:1") ||            // IPv6本地地址
               ipAddress.startsWith("::1") ||                    // IPv6本地地址简化形式
               ipAddress.equals("127.0.0.1") ||                  // 明确指定local IP
               ipAddress.equals("[::1]");                        // IPv6 with brackets
        
        if (isLocal) {
            log.debug("IP {} 被认定为本地IP", ipAddress);
        }
        return isLocal;
    }
}
