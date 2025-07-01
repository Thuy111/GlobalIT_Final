package com.bob.smash.service;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.entity.ChatMessage;

import java.util.List;

public interface ChatService {

    // 채팅방의 모든 메시지 조회
    // 새로고침 후에도 (무조건 세션이 끊김)을 해도 채팅방에 들어갈 수 있도록 메시지 조회
    List<ChatMessageDTO> getMessages(String roomId);

    // 유저 A 또는 B가 포함된 채팅방 목록 조회
    List<ChatRoom> findRoomsByUser(String username);

    // 방을 roomId로 찾는 메서드
    ChatRoomDTO findRoomById(String roomId);

    // 채팅 메시지 저장
    void saveMessage(ChatMessageDTO dto);

    // 1:1 채팅방 찾거나 없으면 생성
    ChatRoomDTO getOrCreateOneToOneRoom(String myUser, String targetUser);

    // 채팅방 메시지 읽음 처리
    void markAsRead(String roomId, String userEmail);

    // (ChatRoom) Entity -> DTO
    default ChatRoomDTO entityToDto(ChatRoom entity) {
        if (entity == null) return null;
        return ChatRoomDTO.builder()
                .roomId(entity.getRoomId())
                .myUser(entity.getMyUser())
                .targetUser(entity.getTargetUser())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
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
                .createdAt(dto.getCreatedAt())
                .build();
    }

    // (ChatMessage) Entity -> DTO
    default ChatMessageDTO entityToDto(ChatMessage chatMessage) {
        if (chatMessage == null) return null;
        return ChatMessageDTO.builder()
                .roomId(chatMessage.getRoomId())
                .sender(chatMessage.getSender())
                .senderNickname(chatMessage.getSenderNickname())
                .message(chatMessage.getMessage())
                .type(chatMessage.getType())
                .time(chatMessage.getTime())
                .isRead(chatMessage.isRead())
                .build();
    }

    // (ChatMessageDTO) DTO -> Entity
    default ChatMessage dtoToEntity(ChatMessageDTO chatMessageDTO) {
        if (chatMessageDTO == null) return null;
        return ChatMessage.builder()
                .roomId(chatMessageDTO.getRoomId())
                .sender(chatMessageDTO.getSender())
                .senderNickname(chatMessageDTO.getSenderNickname())
                .message(chatMessageDTO.getMessage())
                .type(chatMessageDTO.getType())
                .time(chatMessageDTO.getTime())
                .isRead(chatMessageDTO.isRead())
                .build();
    } 
}