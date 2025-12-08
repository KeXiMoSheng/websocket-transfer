package com.jun.websockettransfer.handler;

import com.jun.websockettransfer.cache.WebsocketCache;
import com.jun.websockettransfer.manager.WebSocketClientManager;
import com.jun.websockettransfer.utils.WebsocketUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;

/**
 * 处理用户客户端的连接、消息、断开事件
 */
@Component
public class UserWebSocketServerHandler extends AbstractWebSocketHandler {

    private final WebSocketClientManager clientManager;

    public UserWebSocketServerHandler(WebSocketClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /**
     * 用户端连接建立 → 创建第三方客户端
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, String> params = WebsocketUtil.getParamsFromSession(session);
        if (params == null || params.isEmpty()) {
            System.err.println("用户连接参数为空");
            return;
        }
        System.out.printf("用户[%s]已连接中转服务%n", params.get("userId"));
        WebsocketCache.getUserToServerWebSocketSessionMap().put(params.get("userId"), session);
        // 注册用户会话与第三方websocket会话的映射
        WebsocketCache.getServerWebSocketSessionToUserMap().put(session, params.get("userId"));
        // 创建第三方客户端连接
        clientManager.createThirdPartyClient(session);
    }

    /**
     * 接收用户消息 → 转发到第三方服务器
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = WebsocketCache.getServerWebSocketSessionToUserMap().get(session);
        if (userId == null) {
            System.err.println("用户会话未关联用户ID");
            return;
        }
        // 从缓存中获取第三方会话
        WebSocketSession thirdPartySession = WebsocketCache.getUserToThirdPartyWebSocketSessionMap().get(userId);
        if (thirdPartySession == null) {
            System.err.println("用户未关联第三方会话");
            return;
        }
        // 转发消息到第三方服务器
        WebsocketUtil.sendMessage(thirdPartySession, message);
    }
    /**
     * 处理前端发送的 PCM 音频二进制流
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message){
        //获取前端发送的原始二进制流
//        ByteBuffer byteBuffer = message.getPayload();
//        //将数据存入byte数组
//        byte[] audioBytes = new byte[byteBuffer.remaining()];
//        byteBuffer.get(audioBytes);
        //发送给阿里云服务器
        WebsocketUtil.sendMessage(session, message);
    }
    /**
     * 用户端连接关闭 → 清理第三方连接
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, String> params = WebsocketUtil.getParamsFromSession(session);
        System.out.println("用户["+params.get("userId")+"]断开连接，状态："+status);
        // 清理第三方连接
        clientManager.removeUserSession(session);
    }

    /**
     * 处理用户端连接异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Map<String, String> params = WebsocketUtil.getParamsFromSession(session);
        System.err.printf("用户[%s]连接异常：%s%n", params.get("userId"), exception.getMessage());
        // 清理资源
        clientManager.removeUserSession(session);
    }
}