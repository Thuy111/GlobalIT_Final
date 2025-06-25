package com.bob.smash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bob.smash.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByTimeAsc(String roomId); // 채팅방 ID로 메시지 조회, 시간순 정렬
    
}