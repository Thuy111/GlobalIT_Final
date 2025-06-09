package com.bob.smash.controller;

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
  @GetMapping("/user")
    public OAuth2User getUser(@AuthenticationPrincipal OAuth2User user) {
      // Map<String, Object>
      user.getAttributes().forEach((key, value) -> {
        System.out.println(key + ": " + value);
      });
      return user; // attributes로 구글/카카오 사용자 정보 확인 가능
  }
}
