package com.bob.smash.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bob.smash.dto.ChatRoomDTO;
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.service.ChatService;
import com.bob.smash.service.ChatServiceImpl;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/chat")
public class ChatController {
    private final ChatService chatService;

    //  채팅방 목록 조회
    @GetMapping("/chatList")
    public String chatList(@RequestParam String username, Model model){
        List<ChatRoom> roomList = chatService.findRoomsByUser(username);
        model.addAttribute("roomList", roomList);
        model.addAttribute("username", username);
        model.addAttribute("title", "채팅방 목록");
        return "smash/chat/chatList";
    }

    // 1:1 채팅방 생성
    @PostMapping("/createRoom")
    public String createRoom(Model model,
                            @RequestParam String username,         // 나
                            @RequestParam String targetUsername) { // 상대방
        // 1:1 채팅방 중복 체크 후 없으면 생성
        ChatRoomDTO room = chatService.getOrCreateOneToOneRoom(username, targetUsername);
        model.addAttribute("room", room);
        model.addAttribute("username", username);
        model.addAttribute("targetUsername", targetUsername);
        return "smash/chat/chatRoom";
    }

    // 1:1 채팅방 조회
    @GetMapping("/chatRoom")
    public String chatRoom(Model model,
                        @RequestParam String targetUser, // 상대방 id
                        @RequestParam String myUser) {   // 내 id
        ChatRoomDTO room = chatService.getOrCreateOneToOneRoom(myUser, targetUser);
        model.addAttribute("room", room);
        model.addAttribute("userA", myUser);
        model.addAttribute("userB", targetUser);
        model.addAttribute("title", room.getName());
        // 이전 메시지 로딩
        model.addAttribute("messages", chatService.getMessages(room.getRoomId()));
        return "smash/chat/chatRoom";
    }
}