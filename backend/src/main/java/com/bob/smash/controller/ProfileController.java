package com.bob.smash.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;  
import lombok.RequiredArgsConstructor;  

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.service.ProfileService;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/smash/profile")
    public ResponseEntity<ProfileDTO> getMyPage(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            // 401 오류일 때 문자열이 아니라 ProfileDTO 객체를 반환해야 합니다.
            ProfileDTO errorProfile = ProfileDTO.builder()
                                                .email("N/A")
                                                .nickname("Guest")
                                                .loginType(null)
                                                .isPartner(false)
                                                .profileImageUrl(null)
                                                .build();
            return ResponseEntity.status(401).body(errorProfile); // 프로필 DTO를 반환
        }

        String email = user.getAttribute("email"); // OAuth2User에서 이메일 꺼내기
        String profileImageUrl = null;

         // 카카오 로그인일 경우 프로필 이미지 URL을 다르게 가져옵니다.
        if ("kakao".equals(user.getAuthorities().toString())) {
            profileImageUrl = user.getAttribute("kakao_account.profile.profile_image_url");
        } else {
            profileImageUrl = user.getAttribute("picture"); // OAuth2User에서 프로필 이미지 URL 꺼내기
        }


        // 이메일이 없으면 400 Bad Request로 처리
        if (email == null || email.isEmpty()) {
            ProfileDTO errorProfile = ProfileDTO.builder()
                                                .email("N/A")
                                                .nickname("Unknown")
                                                .loginType(null)
                                                .isPartner(false)
                                                .profileImageUrl(null)
                                                .build();
            return ResponseEntity.badRequest().body(errorProfile); // ProfileDTO 객체 반환
        }

        try {
            ProfileDTO data = profileService.getProfileByEmail(email);

            // 프로필 이미지 URL이 있으면 ProfileDTO에 추가
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                data = ProfileDTO.builder()
                        .email(data.getEmail())
                        .nickname(data.getNickname())
                        .loginType(data.getLoginType())
                        .isPartner(data.isPartner())
                        .profileImageUrl(profileImageUrl) // OAuth2로 가져온 프로필 이미지 URL 추가
                        .build();
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            // 예외 처리 시, ResponseEntity<ProfileDTO> 타입으로 반환
            ProfileDTO errorProfile = ProfileDTO.builder()
                                                .email("Error")
                                                .nickname("Error")
                                                .loginType(null)
                                                .isPartner(false)
                                                .profileImageUrl(null)
                                                .build();
            return ResponseEntity.status(500).body(errorProfile); // ProfileDTO 객체 반환
        }
    }
}