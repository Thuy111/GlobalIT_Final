package com.bob.smash.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.dto.PartnerVerificationResponseDTO;
import com.bob.smash.service.PartnerInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/partner")
public class PartnerInfoController {

    private final PartnerInfoService partnerInfoService;

        private String extractEmail(OAuth2User user) {
        if (user == null) return null;
        String email = user.getAttribute("email");
        if (email == null && user.getAttribute("kakao_account") != null) {
            Map<String, Object> kakao = (Map<String, Object>) user.getAttribute("kakao_account");
            email = (String) kakao.get("email");
        }
        return email;
    }

    // 이메일로 code 조회
    @PostMapping("/code")
    public ResponseEntity<String> getCodeByEmail(@RequestParam String email) {
        if (email == null) return ResponseEntity.status(401).build();
        String code = partnerConversionService.getCodeByEmail(email);
        return ResponseEntity.ok(code);
    }

    // 사업자로 전환
    @PostMapping("/convert")
    public ResponseEntity<PartnerVerificationResponseDTO> convertToPartner(
            @AuthenticationPrincipal OAuth2User user,
            @RequestBody PartnerInfoDTO dto) {

        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();

        PartnerVerificationResponseDTO result = partnerInfoService.verifyAndRegister(email, dto);
        return ResponseEntity.ok(result);
    }

    // 일반 유저로 전환
    @PostMapping("/revert")
    public ResponseEntity<Void> convertToUser(@AuthenticationPrincipal OAuth2User user) {
        String email = extractEmail(user);
        if (email == null) return ResponseEntity.status(401).build();

        partnerInfoService.convertToUser(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateRole(@AuthenticationPrincipal OAuth2User user) {
    String email = extractEmail(user);
    if (email == null) return ResponseEntity.status(401).build();

    partnerInfoService.updateRoleIfPartnerInfoExists(email); // 이 서비스 메서드 만들기
    return ResponseEntity.ok().build();
}
}

