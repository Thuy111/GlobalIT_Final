package com.bob.smash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByTimeAsc(String roomId); // 채팅방 ID로 메시지 조회, 시간순 정렬
    ChatMessage findFirstByRoomIdOrderByTimeDesc(String roomId); // 채팅방 ID로 가장 최근 메시지 조회

    // 채팅방 ID와 사용자 이메일로 메시지 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.sender <> :userEmail AND m.isRead = false")
    List<ChatMessage> findUnreadMessagesByRoomIdAndUser(@Param("roomId") String roomId, @Param("userEmail") String userEmail);
}