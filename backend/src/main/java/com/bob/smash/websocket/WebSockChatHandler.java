package com.bob.smash.websocket;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
// WebSocket 핸들러 클래스 : 핵심 실시간 통신 담당 컴포넌트, roomId별로 WebSocketSession을 관리하여 메시지를 전송
public class WebSockChatHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

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

        // 메시지 타입별 처리
        switch (chatMessage.getType()) {
            case ENTER:
                sessions.add(session);
                chatMessage.setMessage(chatMessage.getSender() + "님 안녕하세요.");
                sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                // (DB저장 필요 없으면 생략)
                break;
            case QUIT:
                sessions.remove(session);
                chatMessage.setMessage(chatMessage.getSender() + "님과의 대화가 종료되었습니다.");
                sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                // (DB저장 필요 없으면 생략)
                break;
            case TALK:
                // DB 저장
                chatService.saveMessage(chatMessage);
                // 모든 세션에 메시지 브로드캐스트
                sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                break;
        }
    }

    private void sendToEachSocket(Set<WebSocketSession> sessions, TextMessage message){
        // 세션이 닫혀있으면 제거하고, 열려있으면 메시지 전송
        Iterator<WebSocketSession> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            WebSocketSession session = iterator.next();
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                } else {
                    // 세션이 닫혀있으면 리스트에서 제거
                    iterator.remove();
                }
            } catch (Exception e) {
                // 전송 중 에러가 나면 세션을 제거 (안전하게)
                // iterator.remove();
                throw new RuntimeException(e);
            }
        }
    }

}