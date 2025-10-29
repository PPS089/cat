package com.example.petcommon.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备识别工具类
 */
@Slf4j
public class DeviceUtil {

    /**
     * 从User-Agent提取设备类型
     */
    public static String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("iphone")) {
            return "iPhone";
        } else if (userAgent.contains("ipad")) {
            return "iPad";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("x11")) {
            return "Unix";
        }
        
        return "Unknown Device";
    }

    /**
     * 从User-Agent提取浏览器信息
     */
    public static String getBrowserInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Browser";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("edg/")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("safari")) {
            return "Safari";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("opera")) {
            return "Opera";
        } else if (userAgent.contains("trident")) {
            return "Internet Explorer";
        }
        
        return "Unknown Browser";
    }

    /**
     * 获取完整的设备标识
     */
    public static String getFullDeviceInfo(HttpServletRequest request) {
        if (request == null) {
            log.debug("HttpServletRequest为null，设备信息为Unknown Device");
            return "Unknown Device";
        }
        
        String userAgent = request.getHeader("User-Agent");
        log.debug("获取到的User-Agent: {}", userAgent);
        
        String deviceType = getDeviceType(userAgent);
        String browser = getBrowserInfo(userAgent);
        
        String fullInfo = String.format("%s - %s", deviceType, browser);
        log.debug("解析出的设备信息: {}", fullInfo);
        return fullInfo;
    }
}
