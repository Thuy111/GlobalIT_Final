package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.bob.smash.dto.MemberDTO;
import com.bob.smash.entity.Member;

import jakarta.servlet.http.HttpServletRequest;

public interface MemberService {
  // 유저 정보 조회
  void checkUser(OAuth2AuthenticationToken user, HttpServletRequest request);

  // 소셜로그인 이메일을 통한 유저정보 DTO 반환
  MemberDTO getCurrentUser(OAuth2AuthenticationToken authentication);

  // 소셜 회원가입 완료
  void completeSocialSignup(HttpServletRequest request, Map<String, String> body);

  // 소셜 로그인 후 전화번호 등록
  void registerPhoneNumber(HttpServletRequest request, Map<String, String> body);

  // 카카오 회원 연동 해제
  void unlinkAndDeleteKakaoMember(String accessToken, MemberDTO currentUser);
  // 구글 회원 연동 해제
  void unlinkAndDeleteGoogleMember(String accessToken, MemberDTO currentUser);

  // Dto → Entity 변환
  default Member dtoToEntity(MemberDTO dto) {
    Member member = Member.builder()
                          .emailId(dto.getEmailId())
                          .region(dto.getRegion())
                          .loginType(dto.getLoginType())
                          .createdAt(LocalDateTime.now())
                          .role(dto.getRole())
                          .tel(dto.getTel())
                          .nickname(dto.getNickname())
                          .build();
    return member;
  }
  // Entity → Dto 변환
  default MemberDTO entityToDto(Member member) {
    MemberDTO dto = MemberDTO.builder()
                             .emailId(member.getEmailId())
                             .region(member.getRegion())
                             .loginType(member.getLoginType())
                             .createdAt(member.getCreatedAt())
                             .role(member.getRole())
                             .tel(member.getTel())
                             .nickname(member.getNickname())
                             .build();
    return dto;
  }
}
