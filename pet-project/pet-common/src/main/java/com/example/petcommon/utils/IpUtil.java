package com.example.petcommon.utils;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类 - 增强版
 * 专门用于获取客户端真实公网IP地址，适配各种代理场景
 * @author 33185
 */
public class IpUtil {
    
    private static final Logger log = LoggerFactory.getLogger(IpUtil.class);

    /**
     * 获取客户端真实IP地址（增强版）
     * 优先获取真实公网IP，过滤本地和无效IP
     * @param request HTTP请求
     * @return IP地址，如果获取不到有效IP返回 "0.0.0.0"
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest为null");
            return "0.0.0.0";
        }
        
        String ip = null;
        
        // 1. 优先从代理头中获取真实IP（适配Nginx等代理场景）
        List<String> proxyHeaders = Arrays.asList(
            "X-Forwarded-For",
            "X-Real-IP", 
            "X-Original-Forwarded-For",
            "X-Originating-IP",
            "X-Remote-IP",
            "X-Remote-Addr",
            "CF-Connecting-IP",  // Cloudflare
            "True-Client-IP",    // Akamai
            "X-Client-IP",
            "X-Forwarded",
            "Forwarded-For",
            "Forwarded",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        );
        
        // 依次检查各个代理头
        for (String header : proxyHeaders) {
            ip = getHeaderValue(request, header);
            if (ip != null && isValidPublicIp(ip)) {
                log.debug("从代理头 {} 获取到有效公网IP: {}", header, ip);
                break;
            }
        }
        
        // 2. 如果没有从代理头获取到有效IP，使用RemoteAddr
        if (ip == null || !isValidPublicIp(ip)) {
            ip = request.getRemoteAddr();
            log.debug("使用RemoteAddr获取IP: {}", ip);
        }
        
        // 3. 处理多级代理情况，取第一个非私有IP
        if (ip != null && ip.contains(",")) {
            String[] ips = ip.split(",");
            for (String candidate : ips) {
                candidate = candidate.trim();
                if (isValidPublicIp(candidate)) {
                    ip = candidate;
                    log.debug("从多级代理 {} 中选择有效IP: {}", Arrays.toString(ips), ip);
                    break;
                }
            }
        }
        
        // 4. 标准化处理
        ip = (ip != null && !ip.isEmpty()) ? ip.trim() : "0.0.0.0";
        
        // 5. 标准化本地测试IP地址
        if (isLocalTestIp(ip)) {
            log.info("检测到本地测试IP: {}", ip);
            // 将IPv6本地回环地址标准化为IPv4格式
            return "127.0.0.1";
        }
        
        if (!isValidPublicIp(ip)) {
            log.warn("获取到的IP {} 不是有效公网IP，标记为无效", ip);
            return "0.0.0.0";
        }
        
        log.info("成功获取客户端真实公网IP: {}", ip);
        return ip;
    }
    
    /**
     * 获取客户端IP地址（兼容旧版本方法）
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        return getClientIp(request);
    }
    
    /**
     * 判断是否为有效的公网IP地址
     * 排除本地地址、私有地址、保留地址
     * @param ip IP地址
     * @return true 如果是有效公网IP，false 否则
     */
    public static boolean isValidPublicIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 排除本地测试IP
        if (isLocalTestIp(ip)) {
            return false;
        }
        
        // 排除私有IP段
        if (isPrivateIp(ip)) {
            return false;
        }
        
        // 排除特殊保留地址
        if (isReservedIp(ip)) {
            return false;
        }
        
        // 基本格式验证
        return isValidIpFormat(ip);
    }
    
    /**
     * 判断是否为本地测试IP
     * @param ip IP地址
     * @return true 如果是本地测试IP，false 否则
     */
    public static boolean isLocalTestIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        return "127.0.0.1".equals(ip) ||
               ip.startsWith("127.") ||
               "localhost".equals(ip) ||
               "0:0:0:0:0:0:0:1".equals(ip) ||
               "::1".equals(ip) ||
               "[::1]".equals(ip) ||
               "0.0.0.0".equals(ip);
    }
    
    /**
     * 判断是否为私有IP地址
     * @param ip IP地址
     * @return true 如果是私有IP，false 否则
     */
    public static boolean isPrivateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4私有地址段
        if (ip.startsWith("10.") ||
            ip.startsWith("192.168.") ||
            ip.startsWith("172.")) {
            
            // 检查172.16.0.0/12 段 (172.16.0.0 - 172.31.255.255)
            if (ip.startsWith("172.")) {
                try {
                    String[] parts = ip.split("\\.");
                    if (parts.length >= 2) {
                        int second = Integer.parseInt(parts[1]);
                        return second >= 16 && second <= 31;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断是否为保留IP地址
     * @param ip IP地址
     * @return true 如果是保留IP，false 否则
     */
    public static boolean isReservedIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 排除特殊保留地址段
        return ip.startsWith("0.") ||
               ip.startsWith("169.254.") ||
               ip.startsWith("224.") ||
               ip.startsWith("240.") ||
               "255.255.255.255".equals(ip);
    }
    
    /**
     * 验证IP地址格式
     * @param ip IP地址
     * @return true 如果格式有效，false 否则
     */
    public static boolean isValidIpFormat(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4格式验证
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }
            
            try {
                for (String part : parts) {
                    int num = Integer.parseInt(part);
                    if (num < 0 || num > 255) {
                        return false;
                    }
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // IPv6格式验证（简化版）
        if (ip.contains(":")) {
            return ip.length() <= 45; // IPv6最大长度
        }
        
        return false;
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