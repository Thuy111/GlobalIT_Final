package com.bob.smash.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

// Websocket을 활성화하기 위한 Config
@RequiredArgsConstructor // Lombok을 사용하여 생성자 주입을 자동으로 생성
@Configuration // 스프링의 설정 클래스로서, WebSocket 관련 설정을 포함
@EnableWebSocket // websocket 서버로서 동작하겠다는 어노테이션
@EnableWebSocketMessageBroker // 메시지 브로커를 활성화하여 메시지 라우팅을 지원
public class WebSockConfig implements WebSocketMessageBrokerConfigurer { 

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS를 사용하여 WebSocket 연결을 지원
        // handler 등록,   js에서 new Websocket할 때 경로 지정
        //다른 url에서도 접속할 수있게(CORS방지)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}