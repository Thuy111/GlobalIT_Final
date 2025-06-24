package com.bob.smash.controller;

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getMyPage(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) return ResponseEntity.status(401).build();
        String email = user.getAttribute("email");
        if (email == null) {
            Map<String, Object> kakao = (Map<String, Object>) user.getAttribute("kakao_account");
            email = (String) kakao.get("email");
        }
        return ResponseEntity.ok(profileService.getProfileByEmail(email));
    }

}