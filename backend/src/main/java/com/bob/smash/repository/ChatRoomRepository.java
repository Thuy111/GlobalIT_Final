package com.bob.smash.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bob.smash.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
  Optional<ChatRoom> findByMyUserAndTargetUser(String myUser, String targetUser); // 유저 A와 B의 조합으로 채팅방 찾기
  Optional<ChatRoom> findByTargetUserAndMyUser(String targetUser, String myUser); // 유저 B와 A의 조합으로 채팅방 찾기 (순서 반대도 체크하기 위함)
  List<ChatRoom> findByMyUserOrTargetUser(String myUser, String targetUser); // 유저 A 또는 B가 포함된 채팅방 찾기
}