package com.bob.smash.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributeAdvice {
    // 전역으로 모델에 테마 정보를 추가하는 어드바이스 클래스
    @ModelAttribute
    public void addThemeToModel(HttpSession session, Model model) {
        String theme = (String) session.getAttribute("theme");
        model.addAttribute("theme", theme != null ? theme : "light");
    }
}
