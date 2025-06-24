package com.bob.smash.controller;

import com.bob.smash.dto.*;
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

    private String extractEmail(OAuth2User user) {
        if (user == null) return null;
        String email = user.getAttribute("email");
        if (email == null && user.getAttribute("kakao_account") != null) {
            Map<String, Object> kakao = (Map<String, Object>) user.getAttribute("kakao_account");
            email = (String) kakao.get("email");
        }
        return email;
    }

    @GetMapping
    public ResponseEntity<ProfileDTO> getMyPage(@AuthenticationPrincipal OAuth2User user) {
        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.getProfileByEmail(email));
    }

    @PutMapping("/member")
    public ResponseEntity<Void> updateMember(@AuthenticationPrincipal OAuth2User user,
                                             @RequestBody UpdateRequestDTO dto) {
        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();
        profileService.updateMember(email, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/partner")
    public ResponseEntity<Void> updatePartner(@AuthenticationPrincipal OAuth2User user,
                                              @RequestBody UpdateRequestDTO dto) {
        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();
        profileService.updatePartner(email, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameCheckResponseDTO> checkNickname(@RequestParam String nickname) {
        boolean isDuplicated = profileService.isNicknameDuplicated(nickname);
        return ResponseEntity.ok(new NicknameCheckResponseDTO(isDuplicated));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<PhoneCheckResponseDTO> checkPhone(@RequestParam String phone) {
        boolean isValid = profileService.isPhoneValid(phone);
        return ResponseEntity.ok(new PhoneCheckResponseDTO(isValid));
    }

    @PostMapping("/image")
    public ResponseEntity<ProfileImageResponseDTO> uploadProfileImage(@AuthenticationPrincipal OAuth2User user,
                                                                       @RequestParam("file") MultipartFile file) {
        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.uploadProfileImage(email, file));
    }

    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteProfileImage(@AuthenticationPrincipal OAuth2User user) {
        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();
        profileService.deleteProfileImage(email);
        return ResponseEntity.ok().build();
    }
}