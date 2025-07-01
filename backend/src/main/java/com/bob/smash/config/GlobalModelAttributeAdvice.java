package com.bob.smash.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bob.smash.dto.CurrentUserDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

// 전역으로 모델에 정보를 추가하는 어드바이스 클래스
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {
    @Value("${front.server.url}")
    private String frontServerUrl;

    // 테마
    @ModelAttribute
    public void addThemeToModel(HttpSession session, Model model) {
        String theme = (String) session.getAttribute("theme");
        model.addAttribute("theme", theme);
    }

    // 현재 로그인된 유저 정보
    @ModelAttribute
    public void addCurrentUserToModel(Model model, HttpSession session, HttpServletRequest request) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        System.out.println("Current User=========[[MODEL: currentUser]]==========" + currentUser);

        String header = request.getHeader("X-Frontend-Auth-Check");
        if ("true".equals(header)) {
            System.out.println("[ModelAttr] 프론트 요청 감지, 세션 검사 패스\"");
            return;
        }
        
        // currentUser가 null이면 모델에 넣지 않음
        if (currentUser == null) {
            System.out.println("[ModelAttr] 프론트 요청 아님, currentUser 없음, 세션 무효화");
            session.invalidate(); // 프론트 요청이 아니면 세션 무효화(프론트 검증 로직은 구현되어 있음)
        }
        model.addAttribute("currentUser", currentUser);
    }
}
