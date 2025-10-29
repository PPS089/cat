package com.example.petservice.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import jakarta.websocket.Session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * WebSocket服务
 */
import lombok.extern.slf4j.Slf4j;

@Component
@ServerEndpoint("/ws/{id}")
@Slf4j
public class WebSocketServer {
    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") Integer id) {
        System.out.println("客户端：" + id + "建立连接");
        sessionMap.put(id.toString(), session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("id") String id) {
        System.out.println("收到来自客户端：" + id + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param id
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {
        System.out.println("连接断开:" + id);
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
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error(e.getMessage());
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
        if (session != null&&session.isOpen()) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
               log.error("发送消息给客户端{}失败", id, e);
            }
        }
    }



}

