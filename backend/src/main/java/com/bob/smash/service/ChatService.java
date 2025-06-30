package com.bob.smash.service;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.entity.ChatMessage;

import java.util.List;

public interface ChatService {

    // 채팅방의 모든 메시지 조회
    List<ChatMessage> getMessages(String roomId);

    // 유저 A 또는 B가 포함된 채팅방 목록 조회
    List<ChatRoom> findRoomsByUser(String username);

    // 방을 roomId로 찾는 메서드
    ChatRoomDTO findRoomById(String roomId);

    // 채팅 메시지 저장
    void saveMessage(ChatMessageDTO dto);

    // 1:1 채팅방 찾거나 없으면 생성
    ChatRoomDTO getOrCreateOneToOneRoom(String myUser, String targetUser);

    // (ChatRoom) Entity -> DTO
    default ChatRoomDTO entityToDto(ChatRoom entity) {
        if (entity == null) return null;
        return ChatRoomDTO.builder()
                .roomId(entity.getRoomId())
                .myUser(entity.getMyUser())
                .targetUser(entity.getTargetUser())
                .name(entity.getName())
                .build();
    }

    // (ChatRoom) DTO -> Entity
    default ChatRoom dtoToEntity(ChatRoomDTO dto) {
        if (dto == null) return null;
        return ChatRoom.builder()
                .roomId(dto.getRoomId())
                .myUser(dto.getMyUser())
                .targetUser(dto.getTargetUser())
                .name(dto.getName())
                .build();
    }

    // (ChatMessage) Entity -> DTO
    default ChatMessage dtoToEntity(ChatMessage chatMessage) {
        if (chatMessage == null) return null;
        return ChatMessage.builder()
                .roomId(chatMessage.getRoomId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .time(chatMessage.getTime())
                .build();
    }
    // (ChatMessage) DTO -> Entity
    default ChatMessage entityToDto(ChatMessage chatMessage) {
        if (chatMessage == null) return null;
        return ChatMessage.builder()
                .roomId(chatMessage.getRoomId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .time(chatMessage.getTime())
                .build();
    }   
}