package com.jun.websockettransfer.handler;

import com.jun.websockettransfer.manager.WebSocketClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 处理用户客户端的连接、消息、断开事件
 */
@Component
public class UserWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketClientManager clientManager;
    
    public UserWebSocketHandler(WebSocketClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /**
     * 用户端连接建立 → 创建第三方客户端
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userSessionId = session.getId();
        System.out.printf("用户[%s]已连接中转服务%n", userSessionId);
        // 创建第三方客户端连接
        clientManager.createThirdPartyClient(session);
    }

    /**
     * 接收用户消息 → 转发到第三方服务器
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userSessionId = session.getId();
        clientManager.forwardToThirdParty(userSessionId, message);
    }

    /**
     * 用户端连接关闭 → 清理第三方连接
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String userSessionId = session.getId();
        System.out.printf("用户[%s]断开连接，状态：%s%n", userSessionId, status);
        // 清理第三方连接
        clientManager.removeUserSession(userSessionId);
    }

    /**
     * 处理用户端连接异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String userSessionId = session.getId();
        System.err.printf("用户[%s]连接异常：%s%n", userSessionId, exception.getMessage());
        // 清理资源
        clientManager.removeUserSession(userSessionId);
    }
}