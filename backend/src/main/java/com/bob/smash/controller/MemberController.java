package com.bob.smash.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/member")
public class MemberController {
  // 유저 정보 조회 API
  @GetMapping("/user")
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User user) {
      if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      return ResponseEntity.ok(user.getAttributes()); // 혹은 MemberDTO로 반환
  }
  // 로그인 정보 조회 API

}
