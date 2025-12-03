package com.jun.websockettransfer.manager;

import com.jun.websockettransfer.client.ThirdPartyWebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维护用户会话与第三方客户端的映射关系，实现消息路由
 */
@Component
public class WebSocketClientManager {

    // 用户Session ID → 第三方客户端映射（线程安全）
    private final Map<String, ThirdPartyWebSocketClient> userToThirdPartyMap = new ConcurrentHashMap<>();
    // 第三方Session → 用户Session ID映射（线程安全）
    private final Map<WebSocketSession, String> thirdPartyToUserMap = new ConcurrentHashMap<>();

    @Value("${third-party.ws.url}")
    private String thirdPartyWsUrl;

    /**
     * 为用户创建并连接第三方WebSocket客户端
     */
    public void createThirdPartyClient(WebSocketSession userSession) {
        String userSessionId = userSession.getId();
        // 先清理旧连接
        removeUserSession(userSessionId);
        
        // 创建新的第三方客户端并连接
        ThirdPartyWebSocketClient client = new ThirdPartyWebSocketClient(thirdPartyWsUrl, this, userSession);
        client.connect();
        userToThirdPartyMap.put(userSessionId, client);
        System.out.printf("为用户[%s]创建第三方客户端，连接地址：%s%n", userSessionId, thirdPartyWsUrl);
    }

    /**
     * 转发用户消息到第三方服务器
     */
    public void forwardToThirdParty(String userSessionId, TextMessage message) {
        ThirdPartyWebSocketClient client = userToThirdPartyMap.get(userSessionId);
        if (client != null && client.getThirdPartySession() != null && client.getThirdPartySession().isOpen()) {
            try {
                client.getThirdPartySession().sendMessage(message);
                System.out.printf("转发用户[%s]消息到第三方：%s%n", userSessionId, message.getPayload());
            } catch (Exception e) {
                System.err.printf("转发用户消息失败：%s%n", e.getMessage());
            }
        } else {
            System.err.printf("用户[%s]的第三方连接未建立%n", userSessionId);
        }
    }

    /**
     * 转发第三方消息到用户端
     */
    public void forwardToUser(WebSocketSession thirdPartySession, TextMessage message) {
        String userSessionId = thirdPartyToUserMap.get(thirdPartySession);
        if (userSessionId == null) {
            System.err.println("未找到第三方会话对应的用户");
            return;
        }

        ThirdPartyWebSocketClient client = userToThirdPartyMap.get(userSessionId);
        if (client != null && client.getUserSession().isOpen()) {
            try {
                client.getUserSession().sendMessage(message);
                System.out.printf("转发第三方消息到用户[%s]：%s%n", userSessionId, message.getPayload());
            } catch (Exception e) {
                System.err.printf("转发第三方消息失败：%s%n", e.getMessage());
            }
        }
    }

    /**
     * 注册第三方会话与用户的映射
     */
    public void registerThirdPartySession(WebSocketSession thirdPartySession, String userSessionId) {
        thirdPartyToUserMap.put(thirdPartySession, userSessionId);
    }

    /**
     * 清理用户会话，关闭对应的第三方连接
     */
    public void removeUserSession(String userSessionId) {
        ThirdPartyWebSocketClient client = userToThirdPartyMap.remove(userSessionId);
        if (client != null) {
            // 关闭第三方连接
            client.close();
            // 移除第三方会话映射
            thirdPartyToUserMap.remove(client.getThirdPartySession());
            System.out.printf("清理用户[%s]的第三方连接%n", userSessionId);
        }
    }

    // 获取用户Session ID（通过第三方会话）
    public String getUserSessionId(WebSocketSession thirdPartySession) {
        return thirdPartyToUserMap.get(thirdPartySession);
    }
}