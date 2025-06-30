package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.entity.ChatMessage;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.repository.ChatMessageRepository;
import com.bob.smash.repository.ChatRoomRepository;
import com.bob.smash.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    // 채팅방의 모든 메시지 조회
    public List<ChatMessage> getMessages(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimeAsc(roomId);
    }

    // 유저 A 또는 B가 포함된 채팅방 목록 조회 (한 명의 유저가 참여한 모든 채팅방)
    public List<ChatRoom> findRoomsByUser(String username) {
        return chatRoomRepository.findByMyUserOrTargetUser(username, username);
    }

    // 방을 roomId로 찾는 메서드 추가
    public ChatRoomDTO findRoomById(String roomId) {
        // chatRooms Map 또는 DB에서 조회
        ChatRoom found = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));
        return entityToDto(found);
    }

    // 1:1 채팅방 찾거나 없으면 생성
    public ChatRoomDTO getOrCreateOneToOneRoom(String myUser, String targetUser) {
        // DB에서 1:1 채팅방 먼저 찾기
        ChatRoom found = chatRoomRepository.findByMyUserAndTargetUser(myUser, targetUser)
            .orElseGet(() -> chatRoomRepository.findByMyUserAndTargetUser(targetUser, myUser).orElse(null));
        if (found != null) return entityToDto(found);

        String targetUserName = memberRepository.findNicknameByEmailId(targetUser); // 상대방의 닉네임 조회

        if (targetUserName==null) {
            targetUserName = targetUser; // 닉네임이 없으면 이메일 ID 사용
        }

        ChatRoom chatRoom = ChatRoom.builder()
            .roomId(UUID.randomUUID().toString())
            .myUser(myUser)
            .targetUser(targetUser)
            .name(targetUserName + "님과의 채팅방")
            .build();
        chatRoomRepository.save(chatRoom);
        return entityToDto(chatRoom);
    }

    // chat 메세지 저장
    @Override
    public void saveMessage(ChatMessageDTO dto) {
        String Nickname = memberRepository.findNicknameByEmailId(dto.getSender());
        if (Nickname == null) {
            Nickname = dto.getSender(); // 닉네임이 없으면 이메일 ID 사용
        }
        ChatMessage entity = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .sender(dto.getSender())
                .senderNickname(Nickname)
                .message(dto.getMessage())
                .type(dto.getType())
                .time(LocalDateTime.now())
                .build();
        chatMessageRepository.save(entity);
    }
}