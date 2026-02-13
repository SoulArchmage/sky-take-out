package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfiguration {

    // ServerEndpointExporter：
    // 用于自动注册WebSocket端点，
    // 使@ServerEndpoint注解的类能被Spring识别并启用WebSocket支持。
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
