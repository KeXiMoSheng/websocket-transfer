package com.jun.websockettransfer.manager;

import com.jun.websockettransfer.cache.WebsocketCache;
import com.jun.websockettransfer.client.ThirdPartyWebSocketClient;
import com.jun.websockettransfer.utils.WebsocketUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * 用于管理用户会话与第三方客户端,创建第三方客户端连接,关闭第三方客户端连接
 */
@Component
public class WebSocketClientManager {

    @Value("${third-party.ws.url}")
    private String thirdPartyWsUrl;

    /**
     * 为用户创建并连接第三方WebSocket客户端
     * @param userSession 用户会话
     */
    public void createThirdPartyClient(WebSocketSession userSession) {
        Map<String, String> paramsFromSession = WebsocketUtil.getParamsFromSession(userSession);
        if (paramsFromSession == null) {
            System.err.println("从用户会话中提取参数失败");
            return;
        }
        String userId = paramsFromSession.get("userId");
        // 先清理旧连接
//        removeUserSession(userId);
        // 创建新的第三方客户端并连接
        ThirdPartyWebSocketClient client = new ThirdPartyWebSocketClient(thirdPartyWsUrl, userId);
        client.connect();
    }

    /**
     * 清理用户会话，关闭对应的第三方连接
     */
    public void removeUserSession(WebSocketSession userSession) {
        Map<String, String> paramsFromSession = WebsocketUtil.getParamsFromSession(userSession);
        if (paramsFromSession == null) {
            System.err.println("从用户会话中提取参数失败");
            return;
        }
        String userId = paramsFromSession.get("userId");
        // 关闭第三方连接
        Map<String, WebSocketSession> userToThirdPartyWebSocketSessionMap = WebsocketCache.getUserToThirdPartyWebSocketSessionMap();
        if (userToThirdPartyWebSocketSessionMap==null||userToThirdPartyWebSocketSessionMap.isEmpty()) {
            System.err.println("用户到第三方WebSocket会话映射为空");
            return;
        }
        WebSocketSession thirdPartySession = userToThirdPartyWebSocketSessionMap.get(userId);
        if (thirdPartySession != null) {
            try {
                thirdPartySession.close();
                System.out.println("成功关闭用户 " + userId + " 的第三方WebSocket连接");
            } catch (Exception e) {
                System.err.println("关闭用户 " + userId + " 的第三方WebSocket连接时出错: " + e.getMessage());
            }
        } else {
            System.err.println("用户 " + userId + " 没有关联的第三方WebSocket会话");
        }
    }
}