package com.example.petcommon.utils;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

/**
 * Token刷新工具类
 * 提供token相关的辅助方法
 */
@Slf4j
public class TokenRefreshUtil {

    /**
     * 从Claims中安全地获取userId
     * 支持多种数据类型的userId（Integer、Long、String等）
     *
     * @param claims JWT claims对象
     * @return 用户ID，如果获取失败返回null
     */
    public static Long extractUserId(Claims claims) {
        if (claims == null) {
            log.warn("Claims为null，无法提取userId");
            return null;
        }

        Long userId = null;

        // 1. 尝试直接获取Long类型的userId
        try {
            userId = claims.get("userId", Long.class);
            if (userId != null) {
                log.debug("成功从claims获取Long类型的userId: {}", userId);
                return userId;
            }
        } catch (Exception e) {
            log.debug("无法获取Long类型的userId: {}", e.getMessage());
        }

        // 2. 尝试获取Integer类型的userId并转换为Long
        try {
            Integer userIdInt = claims.get("userId", Integer.class);
            if (userIdInt != null) {
                userId = userIdInt.longValue();
                log.debug("成功从claims获取Integer类型的userId并转换为Long: {}", userId);
                return userId;
            }
        } catch (Exception e) {
            log.debug("无法获取Integer类型的userId: {}", e.getMessage());
        }

        // 3. 尝试获取Object类型并进行类型转换
        try {
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                } else if (userIdObj instanceof Double) {
                    userId = ((Double) userIdObj).longValue();
                } else if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                }
                if (userId != null) {
                    log.debug("成功从claims获取Object类型的userId并转换为Long: {}", userId);
                    return userId;
                }
            }
        } catch (Exception e) {
            log.debug("无法从Object类型的userId进行转换: {}", e.getMessage());
        }

        // 4. 尝试其他可能的字段名
        String[] fieldNames = {"id", "uid", "user_id"};
        for (String fieldName : fieldNames) {
            try {
                Object idObj = claims.get(fieldName);
                if (idObj != null) {
                    if (idObj instanceof Long) {
                        userId = (Long) idObj;
                    } else if (idObj instanceof Integer) {
                        userId = ((Integer) idObj).longValue();
                    } else {
                        userId = Long.parseLong(idObj.toString());
                    }
                    log.debug("成功从claims字段'{}'获取userId: {}", fieldName, userId);
                    return userId;
                }
            } catch (Exception e) {
                log.debug("无法从claims字段'{}'获取userId: {}", fieldName, e.getMessage());
            }
        }

        log.warn("无法从claims中提取userId，返回null");
        return null;
    }

    /**
     * 从Claims中安全地获取username
     *
     * @param claims JWT claims对象
     * @return 用户名，如果获取失败返回null
     */
    public static String extractUsername(Claims claims) {
        if (claims == null) {
            log.warn("Claims为null，无法提取username");
            return null;
        }

        // 尝试多个可能的字段名
        String[] fieldNames = {"username", "userName", "user_name", "name"};
        for (String fieldName : fieldNames) {
            try {
                String username = claims.get(fieldName, String.class);
                if (username != null && !username.isEmpty()) {
                    log.debug("成功从claims字段'{}'获取username: {}", fieldName, username);
                    return username;
                }
            } catch (Exception e) {
                log.debug("无法从claims字段'{}'获取username: {}", fieldName, e.getMessage());
            }
        }

        log.warn("无法从claims中提取username，返回null");
        return null;
    }
}
