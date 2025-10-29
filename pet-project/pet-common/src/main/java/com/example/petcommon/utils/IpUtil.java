package com.example.petcommon.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP地址工具类
 */
@Slf4j
public class IpUtil {

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest为null");
            return "0.0.0.0";
        }
        
        String ip = null;
        
        // 依次尝试从各个可能的header中获取IP
        ip = getHeaderValue(request, "X-Forwarded-For");
        if (ip == null) {
            ip = getHeaderValue(request, "X-Real-IP");
        }
        if (ip == null) {
            ip = getHeaderValue(request, "Proxy-Client-IP");
        }
        if (ip == null) {
            ip = getHeaderValue(request, "WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = getHeaderValue(request, "HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = getHeaderValue(request, "HTTP_X_FORWARDED_FOR");
        }
        
        // 最后使用RemoteAddr
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多级代理，取第一个IP地址
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        
        ip = (ip != null && !ip.isEmpty()) ? ip : "0.0.0.0";
        
        // 将IPv6本地地址转换为IPv4格式
        ip = normalizeLocalIP(ip);
        
        log.debug("获取到的客户端IP: {}", ip);
        return ip;
    }
    
    /**
     * 标准化本地IP地址
     * 将IPv6本地地址（::1, 0:0:0:0:0:0:0:1）转换为IPv4格式（127.0.0.1）
     * @param ip IP地址
     * @return 标准化后的IP地址
     */
    private static String normalizeLocalIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        
        // IPv6本地地址转换为IPv4
        if (ip.equals("0:0:0:0:0:0:0:1") || 
            ip.equals("::1") || 
            ip.equals("[::1]")) {
            log.debug("将IPv6本地地址 {} 转换为 127.0.0.1", ip);
            return "127.0.0.1";
        }
        
        return ip;
    }
    
    /**
     * 获取请求头值
     * @param request HTTP请求
     * @param headerName 请求头名称
     * @return 请求头值，如果不存在或为unknown则返回null
     */
    private static String getHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
            return value;
        }
        return null;
    }
}