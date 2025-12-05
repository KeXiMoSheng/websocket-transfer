package com.jun.websockettransfer.config;

import com.jun.websockettransfer.handler.UserWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket核心配置，注册用户端连接端点
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private final UserWebSocketHandler userWebSocketHandler;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public WebSocketConfig(UserWebSocketHandler userWebSocketHandler) {
        this.userWebSocketHandler = userWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册用户端连接端点：/ws/proxy
        registry.addHandler(userWebSocketHandler, "/ws/proxy")
                .setAllowedOrigins(allowedOrigins.split(",")); // 跨域配置
    }
}