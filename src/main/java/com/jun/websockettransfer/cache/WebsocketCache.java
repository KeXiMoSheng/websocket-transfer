package com.jun.websockettransfer.cache;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: jun
 * @Date: 2025/12/7 23:49 （日期和时间）
 */
public class WebsocketCache {
    // 用户userId → 第三方客户端websocket会话（线程安全）
    public static final Map<String, WebSocketSession> userToThirdPartyWebSocketSessionMap = new ConcurrentHashMap<>();
    // 第三方Session → 用户userId映射（线程安全）
    public static final Map<WebSocketSession, String> thirdPartyWebSocketSessionToUserMap = new ConcurrentHashMap<>();
    // 用户userId → 中转服务器websocket会话（线程安全）
    public static final Map<String, WebSocketSession> userToServerWebSocketSessionMap = new ConcurrentHashMap<>();
    //中转服务器websocket会话 → 用户userId映射（线程安全）
    public static final Map<WebSocketSession, String> serverWebSocketSessionToUserMap = new ConcurrentHashMap<>();


    public static Map<String, WebSocketSession> getUserToThirdPartyWebSocketSessionMap() {
        return userToThirdPartyWebSocketSessionMap;
    }

    public static Map<WebSocketSession, String> getThirdPartyWebSocketSessionToUserMap() {
        return thirdPartyWebSocketSessionToUserMap;
    }

    public static Map<String, WebSocketSession> getUserToServerWebSocketSessionMap() {
        return userToServerWebSocketSessionMap;
    }

    public static Map<WebSocketSession, String> getServerWebSocketSessionToUserMap() {
        return serverWebSocketSessionToUserMap;
    }

}
