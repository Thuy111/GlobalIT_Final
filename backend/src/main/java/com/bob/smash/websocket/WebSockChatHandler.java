package com.bob.smash.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.service.ChatServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
// WebSocket 핸들러 클래스 : 핵심 실시간 통신 담당 컴포넌트, roomId별로 WebSocketSession을 관리하여 메시지를 전송
public class WebSockChatHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ChatServiceImpl chatService;

    // roomId별 WebSocketSession 관리
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessageDTO chatMessage = objectMapper.readValue(payload, ChatMessageDTO.class);
        String roomId = chatMessage.getRoomId();

        // roomId에 해당하는 세션 set 가져오기
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        Set<WebSocketSession> sessions = roomSessions.get(roomId);

        if (chatMessage.getType().equals(ChatMessageDTO.MessageType.ENTER)) {
            sessions.add(session);
            chatMessage.setMessage(chatMessage.getSender() + "님 안녕하세요.");
            sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        } else if (chatMessage.getType().equals(ChatMessageDTO.MessageType.QUIT)) {
            sessions.remove(session);
            chatMessage.setMessage(chatMessage.getSender() + "님과의 대화가 종료되었습니다.");
            sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        } else {
            sendToEachSocket(sessions, message);
        }
    }

    private void sendToEachSocket(Set<WebSocketSession> sessions, TextMessage message){
        sessions.parallelStream().forEach(roomSession -> {
            try {
                roomSession.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 모든 방에서 해당 세션 제거
        roomSessions.values().forEach(sessions -> sessions.remove(session));
    }
}