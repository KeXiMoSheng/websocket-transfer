package com.jun.websockettransfer.client;

import com.jun.websockettransfer.manager.WebSocketClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.concurrent.ExecutionException;

/**
 * 中转客户端：连接第三方服务器，处理第三方消息转发
 */
public class ThirdPartyWebSocketClient extends AbstractWebSocketHandler {

    private final String thirdPartyWsUrl;
    private final WebSocketClientManager manager;
    private final WebSocketSession userSession;
    private WebSocketSession thirdPartySession; // 第三方服务器会话
    private final StandardWebSocketClient wsClient; // WebSocket客户端
    Logger logger = LoggerFactory.getLogger(ThirdPartyWebSocketClient.class);
    public ThirdPartyWebSocketClient(String thirdPartyWsUrl, WebSocketClientManager manager, WebSocketSession userSession) {
        this.thirdPartyWsUrl = thirdPartyWsUrl;
        this.manager = manager;
        this.userSession = userSession;
        this.wsClient = new StandardWebSocketClient();
    }

    /**
     * 连接第三方服务器
     */
    public void connect() {
        try {
            // 异步连接转为同步（实际生产可优化为异步）
            this.thirdPartySession = wsClient.execute(this, thirdPartyWsUrl).get();
            // 注册第三方会话与用户的映射
            manager.registerThirdPartySession(thirdPartySession, userSession.getId());
            System.out.printf("成功连接第三方服务器：%s%n", thirdPartyWsUrl);
        } catch (InterruptedException | ExecutionException e) {
            System.err.printf("连接第三方服务器失败：%s，原因：%s%n", thirdPartyWsUrl, e.getMessage());
            // 连接失败时关闭用户端连接
            closeUserSession();
        }
    }

    /**
     * 关闭第三方连接
     */
    public void close() {
        if (thirdPartySession != null && thirdPartySession.isOpen()) {
            try {
                thirdPartySession.close();
                System.out.println("关闭第三方服务器连接");
            } catch (Exception e) {
                System.err.printf("关闭第三方连接失败：%s%n", e.getMessage());
            }
        }
    }

    /**
     * 接收第三方服务器文本消息 → 转发到用户端
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        manager.forwardMessageToUser(session, message);
    }
    /**
     * 接收第三方服务器二进制消息 → 转发到用户端
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        //获取前端发送的原始二进制流
//        ByteBuffer byteBuffer = message.getPayload();
//        //将数据存入byte数组
//        byte[] audioBytes = new byte[byteBuffer.remaining()];
//        byteBuffer.get(audioBytes);
        //模拟处理数据
        manager.forwardMessageToUser(session, message);
    }
    /**
     * 第三方连接关闭 → 清理用户连接
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userSessionId = manager.getUserSessionId(session);
        manager.removeUserSession(userSessionId);
        closeUserSession();
        System.out.printf("第三方连接关闭，用户[%s]连接已清理%n", userSessionId);
    }

    /**
     * 处理第三方连接异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.printf("第三方连接异常：%s%n", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * 关闭用户端连接
     */
    private void closeUserSession() {
        if (userSession != null && userSession.isOpen()) {
            try {
                userSession.close(org.springframework.web.socket.CloseStatus.SERVER_ERROR);
            } catch (Exception e) {
                System.err.printf("关闭用户连接失败：%s%n", e.getMessage());
            }
        }
    }

    // Getter
    public WebSocketSession getThirdPartySession() {
        return thirdPartySession;
    }

    public WebSocketSession getUserSession() {
        return userSession;
    }
}