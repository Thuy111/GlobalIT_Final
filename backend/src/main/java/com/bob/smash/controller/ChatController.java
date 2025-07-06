package com.bob.smash.controller;

import java.time.LocalDateTime;
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
import com.bob.smash.dto.FirstChatMessafeDTO;
import com.bob.smash.dto.ReadEventDTO;
import com.bob.smash.entity.ChatMessage;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.service.ChatService;
import com.bob.smash.service.MemberService;
import com.bob.smash.service.PartnerInfoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/chat")
public class ChatController {
    private final ChatService chatService;
    private final MemberService memberService;
    private final PartnerInfoService partnerInfoService;
    private final HttpSession session;

    @Value("${front.server.url}")
    private String frontServerUrl;

    //  채팅방 목록 조회
    @GetMapping("/roomList")
    public String roomList(@RequestParam String user, Model model, HttpSession session) {
        try{
            CurrentUserDTO myAccount = (CurrentUserDTO) session.getAttribute("currentUser");
            if(myAccount == null) {
                return "redirect:" + frontServerUrl + "/profile?error=notLoggedIn";
            }else if(!myAccount.getEmailId().equals(user)) {
                return "redirect:" + frontServerUrl + "/profile?error=invalidUser";
            }

            // 채팅방 목록 조회(멤버 또는 파트너 유저로 조회)
            List<ChatRoomDTO> roomList;
            if (myAccount.getRole() == 0) { // 일반회원
                roomList = chatService.findRoomsByMemberUser(user);
            } else { // 업체회원
                roomList = chatService.findRoomsByPartnerUser(user);
            }
    
            model.addAttribute("roomList", roomList == null ? new ArrayList<>() : roomList);
            model.addAttribute("memberUser", user == null ? "" : user);
            model.addAttribute("title", "채팅방 목록");
            return "smash/chat/roomList";
        }catch (Exception e) {
            throw new RuntimeException("채팅방 목록 조회 실패: " + e.getMessage());
        }
    }

    // 1:1 채팅방 생성
    // @PostMapping("/createRoom")
    // @ResponseBody
    // public ChatRoomDTO createRoom(@RequestBody Map<String, String> params) {
    //     // 순서 : MemberUser, PartnerUser
    //     try{
    //         String memberUser = params.get("memberUser");
    //         String partnerUser = params.get("partnerUser");
    //         ChatRoomDTO room = chatService.getOrCreateOneToOneRoom(memberUser, partnerUser);
    //         return room;
    //     }catch (Exception e) {
    //         throw new RuntimeException("채팅방 생성 실패: " + e.getMessage());
    //     }
    // }
    // 첫 대화 후 채팅방 생성
    @PostMapping("/firstMessage")
    @ResponseBody
    public ChatRoomDTO firstMessage(@RequestBody FirstChatMessafeDTO req) {
        System.out.println("메세지 :::::" + req.getMessage());
        System.out.println("멤버 :::::" + req.getMemberUser());
        System.out.println("파트너 :::::" + req.getPartnerUser());
        System.out.println("타입 :::::" + req.getType());
        System.out.println("보낸사람 :::::" + req.getSender());

        String me;
        String you;

        if(req.getMemberUser().equals(req.getSender())){ // 내가 보낸 경우
            me = req.getMemberUser();
            you = req.getPartnerUser();
        }else{ // 상대방이 보낸 경우
            me = req.getPartnerUser();
            you = req.getMemberUser();
        }

        // 1:1 방이 있으면 찾고, 없으면 생성
        ChatRoomDTO room = chatService.getOrCreateOneToOneRoom(req.getMemberUser(), req.getPartnerUser());
        // 메시지 저장
        ChatMessageDTO message = ChatMessageDTO.builder()
                .message(req.getMessage())
                .type(req.getType())
                .sender(me)
                .time(LocalDateTime.now()) // 현재 시간으로 설정
                .isRead(false) // 처음 메시지는 읽지 않은 상태
                .roomId(room.getRoomId()) // 방 ID 설정
                .build();
        chatService.saveMessage(message);

        // roomId와 메시지 정보 반환
        return room;
    }

    // 첫 대화방 UI (RoomId 없이)
    @GetMapping("/chatRoomInit")
    public String chatRoomInit(@RequestParam String user, Model model) {
        // 세션에서 로그인 정보 확인
        CurrentUserDTO myAccount = (CurrentUserDTO) session.getAttribute("currentUser");
        if (myAccount == null) {
            return "redirect:" + frontServerUrl + "/profile?error=notLoggedIn";
        }

        String myEmail = myAccount.getEmailId();
        // 이미 생성된 방이 있다면 해당 방으로 리다이렉트
        ChatRoomDTO room = chatService.findRoomByMembersAndRole(myEmail, user, myAccount.getRole());
        if (room != null) {
            return "redirect:/smash/chat/chatRoom?roomId=" + room.getRoomId();
        }

        String memberUser;
        String partnerUser; 

        String you;

        if(myAccount.getRole() == 0){ // 일반 사용자
            memberUser = myEmail;
            partnerUser = user;
            // 상대방 업체이름 조회
            you = partnerInfoService.getPartnerInfo(user).getName();
        }else { // 관리자 또는 업체
            partnerUser = myEmail;
            memberUser = user;
            // 상대방 닉네임 조회
            you = memberService.findNicknameByEmail(user);
        }

        if (you == null || you.isEmpty()) {
            you = "탈퇴한 사용자";
        }

        model.addAttribute("room", null); // room은 없음
        model.addAttribute("memberUser", memberUser);
        model.addAttribute("partnerUser", partnerUser);
        model.addAttribute("sender", myEmail);
        model.addAttribute("messages", new ArrayList<>()); // 메시지 없음
        model.addAttribute("title", you);
        return "smash/chat/chatRoom";
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
            System.out.println("내 이메일 ::::::: " + myEmail);

            String memberUser;
            String partnerUser;
            // room DTO에서 상대방 정보 추출
            String you;
            if (room.getMemberUser().equals(myEmail)) { // 내가 멤버인 경우
                partnerUser = room.getPartnerUser();
                memberUser = room.getMemberUser();
                // 상대방 업체이름 조회
                you = partnerInfoService.getPartnerInfo(partnerUser).getName();
            } else if (room.getPartnerUser().equals(myEmail)) { // 내가 파트너인 경우
                partnerUser = room.getMemberUser();
                memberUser = room.getPartnerUser();
                // 상대방 닉네임 조회
                you = memberService.findNicknameByEmail(memberUser);
            } else {
                throw new RuntimeException("이 채팅방에 접근 권한이 없습니다.");
            }

            if (you == null || you.isEmpty()) {
                you = "탈퇴한 사용자";
            }
            
            model.addAttribute("room", room);
            model.addAttribute("memberUser", memberUser);
            model.addAttribute("partnerUser", partnerUser);
            model.addAttribute("sender", myEmail);
            model.addAttribute("messages", chatService.getMessages(roomId));
            model.addAttribute("title", you);
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
    // @MessageMapping("/chat/{roomId}/enter")
    // @SendTo("/topic/chat/{roomId}")
    // public ChatMessageDTO enter(@Payload ChatMessageDTO message, @DestinationVariable String roomId) {
    //     return message;
    // }
    
}