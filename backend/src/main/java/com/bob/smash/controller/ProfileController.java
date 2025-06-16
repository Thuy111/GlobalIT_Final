package com.bob.smash.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;  

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/smash/profile")
    public ResponseEntity<ProfileDTO> getMyPage(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorProfile("N/A", "Guest"));
        }

        String email = user.getAttribute("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorProfile("N/A", "Unknown"));
        }

        try {
            ProfileDTO profile = profileService.getProfileByEmail(email);

            // OAuth2에서 가져온 이미지 URL이 있을 경우 덮어쓰기 (단, DB에 없는 경우만)
            String oauthImage = extractOAuthImage(user);
            if (profile.getProfileImageUrl() == null && oauthImage != null) {
                profile = ProfileDTO.builder()
                        .email(profile.getEmail())
                        .nickname(profile.getNickname())
                        .loginType(profile.getLoginType())
                        .isPartner(profile.isPartner())
                        .profileImageUrl(oauthImage)
                        .bno(profile.getBno())
                        .partnerName(profile.getPartnerName())
                        .build();
            }

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorProfile("Error", "Error"));
        }
    }

    private ProfileDTO createErrorProfile(String email, String nickname) {
        return ProfileDTO.builder()
                .email(email)
                .nickname(nickname)
                .loginType(null)
                .isPartner(false)
                .profileImageUrl(null)
                .build();
    }

    private String extractOAuthImage(OAuth2User user) {
        try {
            if (user.getAttribute("picture") != null) {
                return user.getAttribute("picture"); // Google
            } else if (user.getAttribute("kakao_account") != null) {
                Object profile = ((Map<?, ?>) user.getAttribute("kakao_account")).get("profile");
                if (profile instanceof Map) {
                    return (String) ((Map<?, ?>) profile).get("profile_image_url");
                }
            }
        } catch (Exception e) {
            // ignore and return null
        }
        return null;
    }
}