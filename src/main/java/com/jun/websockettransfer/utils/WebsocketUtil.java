package com.jun.websockettransfer.utils;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: jun
 * @Date: 2025/12/3 22:18 （日期和时间）
 */
public class WebsocketUtil {
    /**
     * 从session中获取客户端params
     * @param session
     * @return Map<String, String>
     */
    public static Map<String, String> getParamsFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        String query = uri.getQuery();
        if (query == null) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                        arr -> arr[0],
                        arr -> arr.length > 1 ? arr[1] : ""
                ));
    }

    /**
     * 向session发送消息
     * @param session
     * @param content
     */
    public static void sendMessage(WebSocketSession session, WebSocketMessage content) {
        try {
            session.sendMessage(new TextMessage(content.getPayload().toString()));
        } catch (Exception e) {
            System.err.printf("第三方发送消息失败：%s%n", e.getMessage());
        }
    }
}
