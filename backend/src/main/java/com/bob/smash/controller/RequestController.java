package com.bob.smash.controller;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/smash/request")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;
    private final MemberRepository memberRepository;

    @GetMapping("/")
    public String request() {return "redirect:/smash/request/listTest";}

    //등록
    @GetMapping("/register")
    public String registerRequest(Model model) {
    model.addAttribute("requestDTO", new RequestDTO());
    return "smash/request/register";
    }

    @PostMapping("/register")
public String registerPost(RequestDTO dto, RedirectAttributes rttr) {

    // Test member giả lập để không cần đăng nhập
    Member member = memberRepository.findByEmailId("test@gmail.com")
        .orElseThrow(() -> new IllegalArgumentException("테스트 회원이 없습니다."));

    Integer idx = requestService.register(dto, member);
    rttr.addFlashAttribute("msg", idx);
    return "redirect:smash/request/listTest";
}
   

    // 요청 상세 보기
    @GetMapping("/{idx}")
    public String getRequestDetail(@PathVariable Integer idx, Model model) {
        RequestDTO dto = requestService.get(idx);
        if (dto == null) {
            return "error/404"; // 요청이 존재하지 않을 경우
        }

        model.addAttribute("request", dto);
        return "redirect:/smash/request/detail"; // templates/request/detail.html
    }
}
