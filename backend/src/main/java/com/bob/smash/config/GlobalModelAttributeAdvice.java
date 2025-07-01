package com.bob.smash.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bob.smash.dto.CurrentUserDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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
        System.out.println("currentUser==========" + currentUser);

        String header = request.getHeader("X-Frontend-Auth-Check");
        String accept = request.getHeader("Accept"); // application/json 감지
        // System.out.println(accept);
        if ("true".equals(header) && accept.contains("json")) { // 프론트 요청 감지
            System.out.println("[ModelAttr] 프론트 요청 감지, 세션 검사 패스");
            return;
        }
        
        // currentUser가 null이면 모델에 넣지 않음
        if (currentUser == null) {
            System.out.println("[ModelAttr] 프론트 요청 아님, currentUser 없음, 세션 무효화");

            // 프론트 요청이 아니면 세션 무효화(프론트 검증 로직은 구현되어 있음) + 구글토큰 세션 미리 임시 저장(api를 끊기 위함)
            SecurityContextHolder.clearContext(); // 스프링 시큐리티 컨텍스트 초기화 (메모리에서 인증을 제거)
            session.removeAttribute("SPRING_SECURITY_CONTEXT"); // 스프링 시큐리티 세션 정보 제거 (세션에서 인증 정보 제거)
        }
        model.addAttribute("currentUser", currentUser);
    }
}
