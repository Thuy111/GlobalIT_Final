package com.bob.smash.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bob.smash.dto.CurrentUserDTO;

import org.springframework.ui.Model;

// 전역으로 모델에 정보를 추가하는 어드바이스 클래스
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {
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
        if(currentUser == null) {
            request.getSession().invalidate(); // 세션 무효화
            // return; // 현재 유저가 없으면 아무것도 추가하지 않음
        }
        model.addAttribute("currentUser", currentUser);
    }
}
