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
            return ResponseEntity.status(401).build();
        }

        String email = user.getAttribute("email"); // OAuth2User에서 이메일 꺼내기

        ProfileDTO data = profileService.getProfileByEmail(email);
        return ResponseEntity.ok(data);
    }
}

