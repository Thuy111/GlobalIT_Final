package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.dto.FirstChatMessafeDTO;
import com.bob.smash.entity.ChatMessage;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.repository.ChatMessageRepository;
import com.bob.smash.repository.ChatRoomRepository;
import com.bob.smash.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Spring WebSocket (STOMP) 메시징 템플릿
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final PartnerInfoService partnerInfo;

    // 유저 A 또는 B가 포함된 채팅방 목록 조회 (한 명의 유저가 참여한 모든 채팅방)
    public List<ChatRoomDTO> findRoomsByUser(String username) {
        List<ChatRoom> rooms = chatRoomRepository.findByMemberUserOrPartnerUser(username, username);
        return rooms.stream()
            .map(this::entityToDto) // Entity -> DTO 변환
            .collect(Collectors.toList());
    }

    // 멤버유저 또는 파트너 유저가 포함된 채팅방 목록 조회
    @Override
    public List<ChatRoomDTO> findRoomsByMemberUser(String memberUser) {
        return chatRoomRepository.findByMemberUser(memberUser)
            .stream().map(this::entityToDto).collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomDTO> findRoomsByPartnerUser(String partnerUser) {
        return chatRoomRepository.findByPartnerUser(partnerUser)
            .stream().map(this::entityToDto).collect(Collectors.toList());
    }

    // role에 따른 채팅방 조회
    @Override
    public ChatRoomDTO findRoomByMembersAndRole(String myEmail, String otherEmail, int myRole) {
        ChatRoom room = null;
        System.out.println(otherEmail + "의 채팅방을 찾습니다. 내 이메일: " + myEmail + ", 내 역할: " + myRole);
        if (myRole == 0) {
            // 일반유저: memberUser=나, partnerUser=상대
            room = chatRoomRepository.findByMemberUserAndPartnerUser(myEmail, otherEmail).orElse(null);
            System.out.println("[일반유저] memberUser=" + myEmail + ", partnerUser=" + otherEmail + " → " + room);
        } else {
            // 업체/관리자: partnerUser=나, memberUser=상대
            room = chatRoomRepository.findByMemberUserAndPartnerUser(otherEmail, myEmail).orElse(null);
            System.out.println("[업체/관리자] memberUser=" + otherEmail + ", partnerUser=" + myEmail + " → " + room);   
        }
        return room != null ? entityToDto(room) : null;
    }

    // 방을 roomId로 찾는 메서드 추가
    public ChatRoomDTO findRoomById(String roomId) {
        // chatRooms Map 또는 DB에서 조회
        ChatRoom found = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));
        return entityToDto(found);
    }

    // 1:1 채팅방 찾거나 없으면 생성
    public ChatRoomDTO getOrCreateOneToOneRoom(String memberUser, String partnerUser) {
        // 상대방 이메일 아이디 유효성 검사
        memberRepository.findByEmailId(partnerUser)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // DB에서 정확히 이 조합만 찾기
        ChatRoom found = chatRoomRepository.findByMemberUserAndPartnerUser(memberUser, partnerUser)
            .orElse(null); // 반대 조합은 절대 조회하지 않음
        if (found != null) return entityToDto(found);

        // 채팅방이 없으면 새로 생성
        ChatRoom chatRoom = ChatRoom.builder()
            .roomId(UUID.randomUUID().toString())
            .memberUser(memberUser)
            .partnerUser(partnerUser)
            .createdAt(LocalDateTime.now()) 
            .build();
        chatRoomRepository.save(chatRoom);
        return entityToDto(chatRoom);
    }

    // chat 메세지 저장
    @Override
    public ChatMessageDTO saveMessage(ChatMessageDTO dto) {
        String Nickname = memberRepository.findNicknameByEmailId(dto.getSender());

        if (Nickname == null) {
            Nickname = dto.getSender(); // 닉네임이 없으면 이메일 ID 사용
        }
        ChatMessage entity = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .sender(dto.getSender())
                .message(dto.getMessage())
                .type(dto.getType())
                .time(LocalDateTime.now())
                .build();
        chatMessageRepository.save(entity);
        return entityToDto(entity); // 저장된 메시지의 DTO 반환
    }

    // 새로고침 후에도 (무조건 세션이 끊김)을 해도 채팅방에 들어갈 수 있도록 메시지 조회
    // 채팅방의 모든 메시지 조회
    @Override
    public List<ChatMessageDTO> getMessages(String roomId) {
        // 1. 메시지 엔티티를 roomId로 조회 (예: JPA 리포지토리)
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimeAsc(roomId);

        // 2. 엔티티를 DTO로 매핑
        return messages.stream()
            .map(this::entityToDto)
            .collect(Collectors.toList());
    }

    // 읽음 처리
    public List<Long> markAsRead(String roomId, String userEmail) {
        // 아직 읽지 않은 메시지 리스트 조회 (예: 자신이 보낸 메시지 X, 방의 모든 메시지 중 읽지 않은 것들)
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByRoomIdAndUser(roomId, userEmail);

        // 각 메시지의 읽음 상태를 변경
        for (ChatMessage msg : unreadMessages) {
            msg.setRead(true); // 사용자별 읽음 여부 처리 (구현 방식에 따라 다름)
        }

        // 일괄 저장
        chatMessageRepository.saveAll(unreadMessages);

        return unreadMessages.stream()
            .map(ChatMessage::getId) // 읽음 처리된 메시지 ID 리스트 반환
            .collect(Collectors.toList());
    }

    // 읽음 이벤트를 WebSocket으로 전송 (STOMP 메시지로 전송)
    public void sendReadEvent(String roomId, List<Long> readMessageIds, String sender) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messageType", "READ");
        payload.put("messageIds", readMessageIds);
        payload.put("sender", sender);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, payload);
    }

    // 읽지않은 메세지 존재 여부
    @Override
    public boolean hasUnreadMessages(String email, int role) {
        List<String> myRoomIds;
        if (role == 0) {
            myRoomIds = chatRoomRepository.findRoomIdsByMemberUser(email);
        } else if (role == 1) {
            myRoomIds = chatRoomRepository.findRoomIdsByPartnerUser(email);
        } else {
            return false;
        }

        if (myRoomIds.isEmpty()) return false;

        return chatMessageRepository.existsUnreadByRoomIdsAndNotSender(myRoomIds, email);
    }

    // (ChatRoom) Entity -> DTO
    public ChatRoomDTO entityToDto(ChatRoom entity) {
        if (entity == null) return null;
        ChatMessage lastMessage = chatMessageRepository.findFirstByRoomIdOrderByTimeDesc(entity.getRoomId());
        ChatMessageDTO lastMessageDTO = lastMessage != null ? entityToDto(lastMessage) : null;
        return ChatRoomDTO.builder()
                .roomId(entity.getRoomId())
                .memberUser(entity.getMemberUser())
                .memberNickname(memberRepository.findNicknameByEmailId(entity.getMemberUser())) // 나의 닉네임 조회
                .partnerUser(entity.getPartnerUser())
                .partnerStoreName(partnerInfo.getStoreNameByEmail(entity.getPartnerUser())) // 상대방의 가게이름 조회
                .name(entity.getName())
                .lastMessage(lastMessageDTO) // 마지막 메시지 내용 조회
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // (ChatRoom) DTO -> Entity
    public ChatRoom dtoToEntity(ChatRoomDTO dto) {
        if (dto == null) return null;
        return ChatRoom.builder()
                .roomId(dto.getRoomId())
                .memberUser(dto.getMemberUser())
                .partnerUser(dto.getPartnerUser())
                .name(dto.getName())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    // (ChatMessage) Entity -> DTO
    public ChatMessageDTO entityToDto(ChatMessage chatMessage) {
        if (chatMessage == null) return null;
        return ChatMessageDTO.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .type(chatMessage.getType())
                .time(chatMessage.getTime())
                .isRead(chatMessage.isRead())
                .build();
    }

    // (ChatMessageDTO) DTO -> Entity
    public ChatMessage dtoToEntity(ChatMessageDTO chatMessageDTO) {
        if (chatMessageDTO == null) return null;
        return ChatMessage.builder()
                .id(chatMessageDTO.getId())
                .roomId(chatMessageDTO.getRoomId())
                .sender(chatMessageDTO.getSender())
                .message(chatMessageDTO.getMessage())
                .type(chatMessageDTO.getType())
                .time(chatMessageDTO.getTime())
                .isRead(chatMessageDTO.isRead())
                .build();
    } 
}