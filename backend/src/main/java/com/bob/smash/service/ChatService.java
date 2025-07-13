package com.bob.smash.service;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.dto.FirstChatMessafeDTO;

import java.util.List;

public interface ChatService {

    // 채팅방의 모든 메시지 조회
    // 새로고침 후에도 (무조건 세션이 끊김)을 해도 채팅방에 들어갈 수 있도록 메시지 조회
    List<ChatMessageDTO> getMessages(String roomId);

    // 멤버유저 또는 파트너 유저가 포함된 채팅방 목록 조회
    List<ChatRoomDTO> findRoomsByMemberUser(String memberUser);
    List<ChatRoomDTO> findRoomsByPartnerUser(String partnerUser);

    // role에 따른 채팅방 조회
    ChatRoomDTO findRoomByMembersAndRole(String myEmail, String otherEmail, int myRole);

    // 유저 A 또는 B가 포함된 채팅방 목록 조회
    List<ChatRoomDTO> findRoomsByUser(String username);

    // 방을 roomId로 찾는 메서드
    ChatRoomDTO findRoomById(String roomId);
    
    // 채팅 메시지 저장
    ChatMessageDTO saveMessage(ChatMessageDTO dto);

    // 1:1 채팅방 찾거나 없으면 생성
    ChatRoomDTO getOrCreateOneToOneRoom(String memberUser, String partnerUser);

    // 채팅방 메시지 읽음 처리
    List<Long> markAsRead(String roomId, String userEmail);

    // 읽음 이벤트를 WebSocket으로 전송
    void sendReadEvent(String roomId, List<Long> readMessageIds, String sender);

    // 읽지 않은 메시지 여부 조회
    boolean hasUnreadMessages(String userEmail, int role);
}