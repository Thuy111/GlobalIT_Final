package com.bob.smash.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bob.smash.dto.MemberDTO;
import com.bob.smash.exception.DuplicateMemberException;
import com.bob.smash.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/member")
public class MemberController {

  private final MemberService memberService;

  // 현재 유저정보 + 유효성 체크
  @GetMapping("/check")
  public ResponseEntity<?> getCheckCurrentUser(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
    if (authentication == null) {
        return ResponseEntity.ok().build(); // 로그인하지 않은 상태도 OK로 처리
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
    }
    
    try {
        memberService.checkUser(authentication, request);  // checkUser 메서드도 OAuth2AuthenticationToken 받도록 수정
        return ResponseEntity.ok(authentication.getPrincipal().getAttributes()); // 혹은 MemberDTO 반환
    } catch (IllegalArgumentException e) {
        return ResponseEntity.ok().build(); // 로그인하지 않은 상태도 OK로
    }
  }


    // 현재 로그인된 유저 정보 조회 + DB 조회
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return ResponseEntity.ok().build(); // 로그인하지 않은 상태도 OK로 처리
            // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
        }

        try {
            MemberDTO memberDTO = memberService.getCurrentUser(authentication);
            return ResponseEntity.ok(memberDTO); // 성공 시 DTO 반환
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

  // 회원 탈퇴
  @DeleteMapping("/delete")
  public String deleteMember(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient, OAuth2AuthenticationToken authentication) {
        // 🔥 access token 바로 사용 가능
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        MemberDTO currentUser = memberService.getCurrentUser(authentication);

        switch (currentUser.getLoginType()) {
            case kakao -> memberService.unlinkAndDeleteKakaoMember(accessToken, currentUser);
            case google -> memberService.unlinkAndDeleteGoogleMember(accessToken, currentUser);
            default -> throw new UnsupportedOperationException("지원하지 않는 로그인 타입입니다.");
        }
    return "회원 탈퇴가 완료되었습니다.";
  }



}
