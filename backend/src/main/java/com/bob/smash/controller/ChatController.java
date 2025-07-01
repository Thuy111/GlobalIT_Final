package com.bob.smash.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
import com.bob.smash.entity.ChatRoom;
import com.bob.smash.service.ChatService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/chat")
public class ChatController {
    private final ChatService chatService;

    @Value("${front.server.url}")
    private String frontServerUrl;

    //  채팅방 목록 조회
    @GetMapping("/roomList")
    public String roomList(@RequestParam String myUser, Model model, HttpSession session) {
        try{
            CurrentUserDTO currntUser = (CurrentUserDTO) session.getAttribute("currentUser");
            if(currntUser == null) {
                return "redirect:" + frontServerUrl + "/profile?error=notLoggedIn";
            }else if(!currntUser.getEmailId().equals(myUser)) {
                return "redirect:" + frontServerUrl + "/profile?error=invalidUser";
            }
    
            List<ChatRoom> roomList = chatService.findRoomsByUser(myUser);
            model.addAttribute("roomList", roomList == null ? new ArrayList<>() : roomList);
            model.addAttribute("myUser", myUser == null ? "" : myUser);
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
            String targetUsername = params.get("targetUsername");
            ChatRoomDTO room = chatService.getOrCreateOneToOneRoom(username, targetUsername);
            return room;
        }catch (Exception e) {
            throw new RuntimeException("채팅방 생성 실패: " + e.getMessage());
        }
    }

    // 1:1 채팅방 조회
    @GetMapping("/chatRoom")
    public String chatRoom(@RequestParam String roomId,
                        @RequestParam String myUser,
                        @RequestParam String targetUser,
                        Model model) {
        try{
            ChatRoomDTO room = chatService.findRoomById(roomId); // roomId로 조회
            model.addAttribute("room", room);
            model.addAttribute("myUser", myUser);
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("messages", chatService.getMessages(roomId));
            model.addAttribute("title", room.getName());
            return "smash/chat/chatRoom";
        }
        catch (Exception e) {
            throw new RuntimeException("채팅방 조회 실패: " + e.getMessage());
        }
    }

    // 새로고침(무조건 세션이 끊김)을 해도 채팅방에 들어갈 수 있도록 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageDTO> getMessages(@PathVariable String roomId) {
        return chatService.getMessages(roomId); // 시간순 정렬해서 반환
    }
    
}