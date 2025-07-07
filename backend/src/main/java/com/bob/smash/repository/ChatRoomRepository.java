package com.bob.smash.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
  Optional<ChatRoom> findByMemberUserAndPartnerUser(String memberUser, String partnerUser); // 멤버유저와 파트너유저 조합으로 채팅방 찾기
  Optional<ChatRoom> findByPartnerUserAndMemberUser(String partnerUser, String memberUser); // 파트너유저와 멤버유저 조합으로 채팅방 찾기 (순서 반대도 체크하기 위함)
  List<ChatRoom> findByMemberUserOrPartnerUser(String memberUser, String partnerUser); // 멤버유저 또는 파트너유저가 포함된 채팅방 전부 찾기
  
  // 멤버 유저 유저로 채팅방 ID 찾기
  @Query("SELECT cr.roomId FROM ChatRoom cr WHERE cr.memberUser = :email")
  List<String> findRoomIdsByMemberUser(@Param("email") String email);
  // 파트너 유저로 채팅방 ID 찾기
  @Query("SELECT cr.roomId FROM ChatRoom cr WHERE cr.partnerUser = :email")
  List<String> findRoomIdsByPartnerUser(@Param("email") String email);

  List<ChatRoom> findByMemberUser(String memberUser); // 멤버 유저로 채팅방 찾기
  List<ChatRoom> findByPartnerUser(String partnerUser); // 파트너 유저로 채팅방 찾기
}