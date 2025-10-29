package com.example.petcommon.utils;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserName = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Long userId = currentUserId.get();
        return userId != null ? userId : 1L; // 默认返回系统用户ID
    }
    
    /**
     * 设置当前用户名称
     */
    public static void setCurrentUserName(String userName) {
        currentUserName.set(userName);
    }
    
    /**
     * 获取当前用户名称
     */
    public static String getCurrentUserName() {
        String userName = currentUserName.get();
        return userName != null ? userName : "system"; // 默认返回系统用户
    }
    
    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        currentUserId.remove();
        currentUserName.remove();
    }
    
    /**
     * 设置完整的用户上下文
     */
    public static void setUserContext(Long userId, String userName) {
        setCurrentUserId(userId);
        setCurrentUserName(userName);
    }
}