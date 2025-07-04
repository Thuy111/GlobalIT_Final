package com.bob.smash.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bob.smash.dto.ChatMessageDTO;
import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.ReadEventDTO;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.service.ChatService;
import com.bob.smash.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/chat")
public class ChatController {
    private final ChatService chatService;
    private final MemberService memberService;
    private final HttpSession session;

    @Value("${front.server.url}")
    private String frontServerUrl;

    //  채팅방 목록 조회
    @GetMapping("/roomList")
    public String roomList(@RequestParam String memberUser, Model model, HttpSession session) {
        try{
            CurrentUserDTO currntUser = (CurrentUserDTO) session.getAttribute("currentUser");
            if(currntUser == null) {
                return "redirect:" + frontServerUrl + "/profile?error=notLoggedIn";
            }else if(!currntUser.getEmailId().equals(memberUser)) {
                return "redirect:" + frontServerUrl + "/profile?error=invalidUser";
            }
    
            List<ChatRoom> roomList = chatService.findRoomsByUser(memberUser);
            model.addAttribute("roomList", roomList == null ? new ArrayList<>() : roomList);
            model.addAttribute("memberUser", memberUser == null ? "" : memberUser);
            model.addAttribute("title", "채팅방 목록");
            return "smash/chat/roomList";
        }catch (Exception e) {
            throw new RuntimeException("채팅방 목록 조회 실패: " + e.getMessage());
        }
    }

    // 1:1 채팅방 생성
    @PostMapping("/createRoom")
    @ResponseBody
    public ChatRoomDTO createRoom(@RequestBody Map<String, String> params) {
        try{
            String username = params.get("username");
            String partnerUsername = params.get("partnerUsername");
            ChatRoomDTO room = chatService.getOrCreateOneToOneRoom(username, partnerUsername);
            return room;
        }catch (Exception e) {
            throw new RuntimeException("채팅방 생성 실패: " + e.getMessage());
        }
    }

    // 1:1 채팅방 조회
    @GetMapping("/chatRoom")
    public String chatRoom(@RequestParam String roomId,
                            Model model) {
        try{
            ChatRoomDTO room = chatService.findRoomById(roomId); // roomId로 조회
            if (room == null) { // 
                throw new RuntimeException("존재하지 않는 채팅방입니다.");
            }
            // 세션에서 로그인 정보 확인
            CurrentUserDTO myAccount = (CurrentUserDTO) session.getAttribute("currentUser");
            if(myAccount == null) {
                return "redirect:" + frontServerUrl + "/profile?error=notLoggedIn";
            }

            String myEmail = myAccount.getEmailId();

            // room DTO에서 상대방 정보 추출
            String partnerUser;
            if (room.getMemberUser().equals(myEmail)) {
                partnerUser = room.getPartnerUser();
            } else if (room.getPartnerUser().equals(myEmail)) {
                partnerUser = room.getMemberUser();
            } else {
                throw new RuntimeException("이 채팅방에 접근 권한이 없습니다.");
            }

            // 상대방 닉네임 조회
            String yourNickname = memberService.findNicknameByEmail(partnerUser);
            if (yourNickname == null || yourNickname.isEmpty()) {
                yourNickname = "탈퇴한 사용자";
            }
            
            model.addAttribute("room", room);
            model.addAttribute("memberUser", myEmail);
            model.addAttribute("partnerUser", partnerUser);
            model.addAttribute("messages", chatService.getMessages(roomId));
            model.addAttribute("title", yourNickname);
            return "smash/chat/chatRoom";
        }
        catch (Exception e) {
            throw new RuntimeException("채팅방 조회 실패: " + e.getMessage());
        }
    }

    // 새로고침(무조건 세션이 끊김)을 해도 채팅방에 들어갈 수 있도록 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    @ResponseBody
    public List<ChatMessageDTO> getMessages(@PathVariable String roomId) {
        return chatService.getMessages(roomId);
    }

    // 채팅방 메시지 읽음 처리 (DB + STOMP)
    @MessageMapping("/chat/{roomId}/read")
    public void readMessages(@Payload ReadEventDTO request, @DestinationVariable String roomId) {
        // 1. DB에서 읽음 처리
        List<Long> readMessageIds = chatService.markAsRead(roomId, request.getSender());
        // 2. 새로 읽은 메시지 ID, 읽은 유저 정보를 브로드캐스트
        chatService.sendReadEvent(roomId, readMessageIds, request.getSender());
    }

    // 채팅 메시지 전송 (STOMP)
    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO message, @DestinationVariable String roomId) {
        ChatMessageDTO saved = chatService.saveMessage(message); // 메시지 저장
        // 2. id가 포함된 saved 객체를 브로드캐스트
        return saved;
    }
    // 채팅방 입장 (STOMP)
    @MessageMapping("/chat.enter")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageDTO enter(@Payload ChatMessageDTO message, @DestinationVariable String roomId) {
        
        return message;
    }
    
}