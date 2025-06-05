package com.bob.smash.controller;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/smash/request")
@RequiredArgsConstructor
public class RequestController {

    //자동 주입
    private final RequestService requestService; 

    @GetMapping("/")
  public String estimate() {
    return "redirect:/smash/request/list";
  }

  //등록
    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("requestDTO", new RequestDTO());
        return "smash/request/register";
    }

    @PostMapping("/register")
    public String submitForm(@ModelAttribute RequestDTO requestDTO, Principal principal) {
        // TODO: 실제 로그인된 사용자로부터 Member 객체 조회
        Member dummyMember = Member.builder().emailId("test@example.com").build(); // 임시 코드
        requestService.register(requestDTO, dummyMember);
        return "redirect:/smash/request/list"; // 저장 후 홈 또는 목록으로 이동
    }
}
