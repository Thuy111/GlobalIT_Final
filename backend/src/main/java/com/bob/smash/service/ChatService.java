package com.bob.smash.service;

import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.entity.ChatMessage;

import java.util.List;

public interface ChatService {

    // 채팅방의 모든 메시지 조회
    List<ChatMessage> getMessages(String roomId);

    // 유저 A 또는 B가 포함된 채팅방 목록 조회
    List<ChatRoom> findRoomsByUser(String username);

    // 채팅 메시지 저장
    void saveMessage(String roomId, String sender, String message);

    // 1:1 채팅방 찾거나 없으면 생성
    ChatRoomDTO getOrCreateOneToOneRoom(String userA, String userB);

    // Entity -> DTO
    default ChatRoomDTO entityToDto(ChatRoom entity) {
        if (entity == null) return null;
        return ChatRoomDTO.builder()
                .roomId(entity.getRoomId())
                .userA(entity.getUserA())
                .userB(entity.getUserB())
                .name(entity.getName())
                .build();
    }

    // DTO -> Entity
    default ChatRoom dtoToEntity(ChatRoomDTO dto) {
        if (dto == null) return null;
        return ChatRoom.builder()
                .roomId(dto.getRoomId())
                .userA(dto.getUserA())
                .userB(dto.getUserB())
                .name(dto.getName())
                .build();
    }
}