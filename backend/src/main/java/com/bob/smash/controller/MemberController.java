package com.bob.smash.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/member")
public class MemberController {

  private final MemberRepository memberRepository;

  // 유저 정보 조회 API
  @GetMapping("/check")
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User user) {
      if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      return ResponseEntity.ok(user.getAttributes()); // 혹은 MemberDTO로 반환
  }

  @GetMapping("/auth/session-info")
  public ResponseEntity<?> getSessionInfo(HttpServletRequest request) {
      String email = (String) request.getSession().getAttribute("social_email");
      String provider = (String) request.getSession().getAttribute("social_provider");
      String nickname = (String) request.getSession().getAttribute("nickname");

      if (email == null || provider == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
      }

      return ResponseEntity.ok(Map.of(
          "email", email,
          "provider", provider,
          "nickname", nickname
      ));
  }

  @PostMapping("/auth/complete-social")
  public ResponseEntity<?> completeSocialSignup(
          HttpServletRequest request,
          @RequestBody Map<String, String> body
  ) {
      // 프론트에서 보낸 정보 받기
      String email = body.get("email");
      String provider = body.get("provider");
      String nickname = body.get("nickname");
      String phone = body.get("phone");

      if (email == null || provider == null || phone == null) {
          return ResponseEntity.badRequest().body("정보 부족");
      }

      Optional<Member> existMemberWithEmail = memberRepository.findByEmailId(email);
      Optional<Member> existMemberWithTel = memberRepository.findByTel(phone);

      boolean existMember = existMemberWithEmail.isPresent() && existMemberWithTel.isPresent(); // 이메일과 전화번호 모두 존재하는지 확인

      if (existMember) {
          return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 사용자");
      }

      Member.LoginType loginType = "kakao".equals(provider) ?
              Member.LoginType.kakao : Member.LoginType.google;

      Member newMember = Member.builder()
              .emailId(email)
              .nickname(nickname)
              .createdAt(LocalDateTime.now())
              .loginType(loginType)
              .role((byte) 0)
              .tel(phone)
              .build();

      memberRepository.save(newMember);
      
      return ResponseEntity.ok("회원가입 완료");
  }



}
