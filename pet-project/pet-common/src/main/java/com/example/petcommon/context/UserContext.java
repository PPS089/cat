package com.example.petcommon.context;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserName = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentAdminShelterId = new ThreadLocal<>();
    
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
        return currentUserId.get();
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
     * 设置当前用户角色
     */
    public static void setCurrentUserRole(String role) {
        currentUserRole.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }

    /**
     * 设置当前管理员绑定的收容所ID（仅管理员有效）
     */
    public static void setCurrentAdminShelterId(Integer shelterId) {
        currentAdminShelterId.set(shelterId);
    }

    /**
     * 获取当前管理员绑定的收容所ID
     */
    public static Integer getCurrentAdminShelterId() {
        return currentAdminShelterId.get();
    }
    
    /**
     * 是否为平台超级管理员（ADMIN 且未绑定收容所）
     */
    public static boolean isPlatformAdmin() {
        String role = getCurrentUserRole();
        Integer shelterId = getCurrentAdminShelterId();
        return "ADMIN".equalsIgnoreCase(role) && shelterId == null;
    }

    /**
     * 是否为收容所管理员（ADMIN 且绑定了收容所）
     */
    public static boolean isShelterAdmin() {
        String role = getCurrentUserRole();
        Integer shelterId = getCurrentAdminShelterId();
        return "ADMIN".equalsIgnoreCase(role) && shelterId != null;
    }
    
    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        currentUserId.remove();
        currentUserName.remove();
        currentUserRole.remove();
        currentAdminShelterId.remove();
    }
    
    /**
     * 设置完整的用户上下文
     */
    public static void setUserContext(Long userId, String userName) {
        setCurrentUserId(userId);
        setCurrentUserName(userName);
    }

    public static void setUserContext(Long userId, String userName, String role) {
        setCurrentUserId(userId);
        setCurrentUserName(userName);
        setCurrentUserRole(role);
    }

    public static void setUserContext(Long userId, String userName, String role, Integer adminShelterId) {
        setCurrentUserId(userId);
        setCurrentUserName(userName);
        setCurrentUserRole(role);
        setCurrentAdminShelterId(adminShelterId);
    }
}