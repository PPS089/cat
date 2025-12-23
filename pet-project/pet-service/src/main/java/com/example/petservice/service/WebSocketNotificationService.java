package com.example.petservice.service;

import com.example.petservice.websocket.WebSocketServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket通知服务
 * 用于向管理员实时推送领养和寄养申请等通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 向所有在线管理员发送通知
     * @param type 通知类型
     * @param title 通知标题
     * @param message 通知内容
     * @param data 附加数据
     */
    public void notifyAdmins(String type, String title, String message, Object data) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("title", title);
            notification.put("message", message);
            notification.put("data", data);
            notification.put("timestamp", LocalDateTime.now().toString());

            String jsonMessage = objectMapper.writeValueAsString(notification);
            WebSocketServer.broadcastToAdmins(jsonMessage);

            log.info("向所有管理员发送通知: type={}, title={}", type, title);
        } catch (JsonProcessingException e) {
            log.error("序列化通知消息失败", e);
        }
    }
    
    /**
     * 向指定收容所的管理员发送通知（包括平台管理员）
     * @param shelterId 收容所ID，为null时只发给平台管理员
     * @param type 通知类型
     * @param title 通知标题
     * @param message 通知内容
     * @param data 附加数据
     */
    public void notifyAdminsToShelter(Integer shelterId, String type, String title, String message, Object data) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("title", title);
            notification.put("message", message);
            notification.put("data", data);
            notification.put("timestamp", LocalDateTime.now().toString());
            
            String jsonMessage = objectMapper.writeValueAsString(notification);

            // 只发给：平台管理员 + 指定收容所管理员（其他收容所管理员不接收）
            WebSocketServer.broadcastToShelter(shelterId, jsonMessage);
            if (shelterId == null) {
                log.info("向平台管理员发送通知: type={}, title={}", type, title);
            } else {
                log.info("向收容所[{}]的管理员发送通知: type={}, title={}", shelterId, type, title);
            }
        } catch (JsonProcessingException e) {
            log.error("序列化通知消息失败", e);
        }
    }
    
    /**
     * 发送新的领养申请通知
     * @param adoptionId 领养记录ID
     * @param petName 宠物名称
     * @param applicantName 申请人姓名
     * @param shelterId 收容所ID
     */
    public void sendNewAdoptionNotification(Integer adoptionId, String petName, String applicantName, Integer shelterId) {
        Map<String, Object> data = new HashMap<>();
        data.put("adoptionId", adoptionId);
        data.put("petName", petName);
        data.put("applicantName", applicantName);
        data.put("applicationType", "adoption");
        data.put("shelterId", shelterId);
        
        notifyAdminsToShelter(shelterId, "new_application", "新的领养申请", 
                   String.format("用户 %s 申请领养宠物 %s", applicantName, petName), 
                   data);
    }
    
    /**
     * 发送新的寄养申请通知
     * @param fosterId 寄养记录ID
     * @param petName 宠物名称
     * @param applicantName 申请人姓名
     * @param shelterId 收容所ID
     */
    public void sendNewFosterNotification(Integer fosterId, String petName, String applicantName, Integer shelterId) {
        Map<String, Object> data = new HashMap<>();
        data.put("fosterId", fosterId);
        data.put("petName", petName);
        data.put("applicantName", applicantName);
        data.put("applicationType", "foster");
        data.put("shelterId", shelterId);
        
        notifyAdminsToShelter(shelterId, "new_application", "新的寄养申请", 
                   String.format("用户 %s 申请寄养宠物 %s", applicantName, petName), 
                   data);
    }
}
