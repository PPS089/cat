package com.example.petservice.websocket;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

@Component
@ServerEndpoint("/ws/{id}")
@Slf4j
public class WebSocketServer {
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        log.info("客户端: {} 建立连接", id);
        sessionMap.put(id, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("id") String id) {
        log.info("收到来自客户端 {} 的信息: {}", id, message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param id
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {
        log.info("连接断开: {}", id);
        sessionMap.remove(id);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (java.io.IOException e) {
                log.error("发送消息失败", e);
            } catch (IllegalStateException e) {
                log.error("WebSocket连接状态异常", e);
                sessionMap.entrySet().removeIf(entry -> entry.getValue().equals(session));
            }
        }
    }
    /**
     * 特定客户端群发
     *
     * @param message
     */
    public void sendToUsersClient(String id, String message) {
        Session session = sessionMap.get(id);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (java.io.IOException e) {
                log.error("发送消息给客户端{}失败", id, e);
            } catch (IllegalStateException e) {
                log.error("客户端{}的WebSocket连接状态异常", id, e);
                sessionMap.remove(id);
            }
        }
    }

    @jakarta.websocket.OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，sessionId={}, error={}", session != null ? session.getId() : "null", error.getMessage(), error);
    }



}

