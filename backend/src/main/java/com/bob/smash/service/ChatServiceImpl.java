package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.entity.ChatMessage;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.repository.ChatMessageRepository;
import com.bob.smash.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private Map<String, ChatRoomDTO> chatRooms = new HashMap<>();

    // 채팅방의 모든 메시지 조회
    public List<ChatMessage> getMessages(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimeAsc(roomId);
    }

    // 유저 A 또는 B가 포함된 채팅방 목록 조회 (한 명의 유저가 참여한 모든 채팅방)
    public List<ChatRoom> findRoomsByUser(String username) {
        return chatRoomRepository.findByUserAOrUserB(username, username);
    }

    // 채팅 메시지 저장
    public void saveMessage(String roomId, String sender, String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(sender);
        chatMessage.setMessage(message);
        chatMessage.setTime(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
    }

    // 1:1 채팅방 찾거나 없으면 생성
    public ChatRoomDTO getOrCreateOneToOneRoom(String userA, String userB) {
        for (ChatRoomDTO room : chatRooms.values()) {
            if ((room.getUserA().equals(userA) && room.getUserB().equals(userB)) ||
                (room.getUserA().equals(userB) && room.getUserB().equals(userA))) {
                return room;
            }
        }
        // 없으면 새로 생성
        String randomId = UUID.randomUUID().toString();
        ChatRoomDTO chatRoom = ChatRoomDTO.builder()
                .roomId(randomId)
                .userA(userA)
                .userB(userB)
                .name(userB + "님과의 채팅방") // 채팅방 이름은 상대방 이름으로 설정
                .build();
        chatRooms.put(randomId, chatRoom);
        return chatRoom;
    }
}