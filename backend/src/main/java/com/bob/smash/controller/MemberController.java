package com.bob.smash.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bob.smash.dto.MemberDTO;
import com.bob.smash.service.MemberService;
import com.bob.smash.service.MemberServiceImpl.DuplicateMemberException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/member")
public class MemberController {

  private final MemberService memberService;

  // 현재 유저정보 + 유효성 체크
  @GetMapping("/check")
  public ResponseEntity<?> getCheckCurrentUser(@AuthenticationPrincipal OAuth2User user, HttpServletRequest request) {
      if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      try{
        memberService.checkUser(user, request);
        return ResponseEntity.ok(user.getAttributes());// 혹은 MemberDTO로 반환
      }catch(IllegalArgumentException e){
        return ResponseEntity.ok().build(); // 로그인 하지 않은 상태에도 오류를 띄우지 않기 위해 OK로 처리
      }
  }

  // 현재 로그인된 유저 정보 조회 + DB 조회
  @GetMapping("/currnet-user")
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User user, HttpServletRequest request) {
      if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      try {
        MemberDTO memberDTO = memberService.getCurrentUser(user, null); // body는 사용하지 않으니 null 가능
        return ResponseEntity.ok(memberDTO); // DTO 반환
      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
      }
  }

  // 소셜로그인 번호만 등록
  @PostMapping("/auth/register-phone")
  public ResponseEntity<?> registerPhoneNumber(
    @RequestBody Map<String, String> body,
    HttpServletRequest request
  ) {
      try {
          memberService.registerPhoneNumber(request, body);
          return ResponseEntity.ok("전화번호가 등록되었습니다.");
      } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().body(e.getMessage());
      }
  }

  // 소셜 로그인 세션 정보 조회
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

  // 소셜 로그인 회원가입 완료 처리
  @PostMapping("/auth/complete-social")
  public ResponseEntity<?> completeSocialSignup(
    @RequestBody Map<String, String> body,
    HttpServletRequest request
  ) {
      try {
          memberService.completeSocialSignup(request, body);
          return ResponseEntity.ok("회원가입이 완료되었습니다.");
      } catch (DuplicateMemberException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 중복된 회원가입 시
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      }
  }



}
