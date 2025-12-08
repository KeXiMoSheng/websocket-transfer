package com.jun.websockettransfer.client;

import com.jun.websockettransfer.cache.WebsocketCache;
import com.jun.websockettransfer.utils.WebsocketUtil;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.concurrent.ExecutionException;

/**
 * 中转客户端：连接第三方服务器，处理第三方消息转发
 */
public class ThirdPartyWebSocketClient extends AbstractWebSocketHandler {

    private final String thirdPartyWsUrl;
    private final StandardWebSocketClient wsClient; // WebSocket客户端
    private final String userId;
    public ThirdPartyWebSocketClient(String thirdPartyWsUrl, String userId) {
        this.thirdPartyWsUrl = thirdPartyWsUrl;
        this.wsClient = new StandardWebSocketClient();
        this.userId = userId;
    }

    /**
     * 连接第三方服务器
     */
    public void connect() {
        try {
            // 异步连接转为同步（实际生产可优化为异步）
            WebSocketSession webSocketSession = wsClient.execute(this, thirdPartyWsUrl).get();
            // 注册第三方会话与用户的映射
            WebsocketCache.getUserToThirdPartyWebSocketSessionMap().put(userId,webSocketSession);
            WebsocketCache.getThirdPartyWebSocketSessionToUserMap().put(webSocketSession, userId);
            System.out.printf("成功连接第三方服务器：%s%n", thirdPartyWsUrl);
        } catch (InterruptedException | ExecutionException e) {
            System.err.printf("连接第三方服务器失败：%s，原因：%s%n", thirdPartyWsUrl, e.getMessage());
            // 连接失败时关闭用户端连接
//            closeUserSession();
        }
    }


    /**
     * 接收第三方服务器文本消息 → 转发到用户端
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//        WebsocketUtil.sendMessage(session, message);
        // 从缓存中获取用户会话
        WebSocketSession userSession = WebsocketCache.getUserToServerWebSocketSessionMap().get(userId);
        if (userSession == null) {
            System.err.println("用户未关联用户会话");
            return;
        }
        // 转发消息到用户端
        WebsocketUtil.sendMessage(userSession, message);
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
        WebsocketUtil.sendMessage(session, message);
    }
    /**
     * 第三方连接关闭 → 清理用户连接
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        String userSessionId = manager.getUserSessionId(session);
//        manager.removeUserSession(userSessionId);
        System.out.printf("第三方连接关闭，用户[%s]连接已清理%n", session);
    }

    /**
     * 处理第三方连接异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.printf("第三方连接异常：%s%n", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }


}