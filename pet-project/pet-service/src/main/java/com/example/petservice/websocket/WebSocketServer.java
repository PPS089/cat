package com.example.petservice.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * @author 33185
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{id}")
public class WebSocketServer {

    /**
     * 内部类：封装 Session + 管理员收容所 ID
     */
    public static class SessionInfo {
        public final Session session;
        public boolean isAdmin; // 仅当收到 auth 消息后为 true
        public Integer adminShelterId; // null 表示平台管理员，非 null 表示收容所管理员

        public SessionInfo(Session session) {
            this.session = session;
            this.isAdmin = false;
        }
    }

    /** id -> SessionInfo，一个 id 只能同时在线一个连接（自动踢旧） */
    private static final Map<String, SessionInfo> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        // 直接覆盖旧连接（同 id 踢掉上一个）
        SessionInfo old = ONLINE_SESSIONS.put(id, new SessionInfo(session));
        if (old != null && old.session.isOpen()) {
            try {
                old.session.close(); // 可选：强制关闭旧连接
            } catch (IOException e) {
                log.warn("[WS] 关闭旧连接失败: id={}", id, e);
            }
        }
        // 发送结构化欢迎消息
        send(id, "{\"type\":\"welcome\"}");
        log.info("[WS] {} 在线，当前总数：{}", id, ONLINE_SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("id") String id) {
        // 心跳快速返回
        if ("ping".equalsIgnoreCase(message)) {
            send(id, "pong");
            return;
        }
        
        // 处理认证消息：{"type":"auth","adminShelterId":123}
        try {
            if (message.startsWith("{")) {
                // 尝试解析JSON
                int typeIdx = message.indexOf("\"type\"");
                if (typeIdx > 0) {
                    int authIdx = message.indexOf("\"auth\"", typeIdx);
                    if (authIdx > 0) {
                        // 提取adminShelterId
                        int shelterIdIdx = message.indexOf("\"adminShelterId\"");
                        if (shelterIdIdx > 0) {
                            int colonIdx = message.indexOf(":", shelterIdIdx);
                            int commaIdx = message.indexOf(",", colonIdx);
                            int braceIdx = message.indexOf("}", colonIdx);
                            int endIdx = commaIdx > 0 ? commaIdx : braceIdx;
                            if (endIdx > colonIdx) {
                                String valueStr = message.substring(colonIdx + 1, endIdx).trim();
                                if (!"null".equals(valueStr)) {
                                    try {
                                        Integer adminShelterId = Integer.parseInt(valueStr);
                                        setAdminShelterId(id, adminShelterId);
                                        send(id, "{\"type\":\"auth_ok\"}");
                                        return;
                                    } catch (NumberFormatException ignored) {
                                        // 忽略解析错误
                                    }
                                } else {
                                    // adminShelterId 为 null，设置为平台管理员
                                    setAdminShelterId(id, null);
                                    send(id, "{\"type\":\"auth_ok\"}");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[WS] 解析认证消息失败: {}", e.getMessage());
        }
        
        log.info("[WS] 收到 {} => {}", id, message);
        // TODO：在这里处理你的业务消息（聊天、指令、认证等）
    }

    @OnClose
    public void onClose(@PathParam("id") String id) {
        SessionInfo sessionInfo = ONLINE_SESSIONS.remove(id);
        if (sessionInfo != null) {
            try {
                sessionInfo.session.close();
            } catch (IOException ignored) {
                // 忽略关闭异常
            }
        }
        log.info("[WS] {} 离线，剩余：{}", id, ONLINE_SESSIONS.size());
    }


    @OnError
    public void onError(Session session, Throwable t) {
        log.error("[WS] 连接异常", t);
        // 异常也会触发 @OnClose，无需重复 remove
    }

    // ====================== 对外推送接口 ======================

    /** 给指定 id 推送消息 */
    public static void send(String id, String message) {
        SessionInfo info = ONLINE_SESSIONS.get(id);
        if (info != null && info.session.isOpen()) {
            info.session.getAsyncRemote().sendText(message);
        }
    }

    /** 广播给所有人 */
    public static void broadcast(String message) {
        ONLINE_SESSIONS.values().forEach(info -> {
            if (info.session.isOpen()) {
                info.session.getAsyncRemote().sendText(message);
            }
        });
    }

    /**
     * 根据收容所ID定向推送（只给对应收容所管理员 + 平台管理员）
     * @param shelterId 收容所ID，为 null 表示只给平台管理员
     * @param message 消息内容
     */
    public static void broadcastToShelter(Integer shelterId, String message) {
        ONLINE_SESSIONS.values().forEach(info -> {
            if (info.session.isOpen()) {
                if (!info.isAdmin) {
                    return;
                }
                // 平台管理员（adminShelterId == null）收全部
                if (info.adminShelterId == null) {
                    info.session.getAsyncRemote().sendText(message);
                }
                // 收容所管理员：只收对应收容所的消息
                else if (shelterId != null && Objects.equals(info.adminShelterId, shelterId)) {
                    info.session.getAsyncRemote().sendText(message);
                }
            }
        });
    }

    /**
     * 只给已完成认证的管理员推送（平台管理员 + 所有收容所管理员）
     * @param message 消息内容
     */
    public static void broadcastToAdmins(String message) {
        ONLINE_SESSIONS.values().forEach(info -> {
            if (info.isAdmin && info.session.isOpen()) {
                info.session.getAsyncRemote().sendText(message);
            }
        });
    }

    /**
     * 设置某个连接的管理员收容所ID（由业务层调用）
     * @param id 连接 ID（一般是 userId）
     * @param adminShelterId 管理员绑定的收容所ID，null 表示平台管理员
     */
    public static void setAdminShelterId(String id, Integer adminShelterId) {
        SessionInfo info = ONLINE_SESSIONS.get(id);
        if (info != null) {
            info.isAdmin = true;
            info.adminShelterId = adminShelterId;
            log.info("[WS] 设置连接 {} 的 adminShelterId = {}", id, adminShelterId);
        }
    }

    /** 获取当前在线人数 */
    public static int getOnlineCount() {
        return (int) ONLINE_SESSIONS.values().stream().filter(info -> info.session.isOpen()).count();
    }

    /** 获取指定 id 是否在线 */
    public static boolean isOnline(String id) {
        SessionInfo info = ONLINE_SESSIONS.get(id);
        return info != null && info.session.isOpen();
    }
}
