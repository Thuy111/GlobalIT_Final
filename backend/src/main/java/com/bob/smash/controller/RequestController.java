package com.bob.smash.controller;

//추후 각자 DTO 통일할 것
//찬영
import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.bob.smash.entity.Request;
//탄튀
import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import java.util.List;

@RestController
@RequestMapping("/smash/requests")
@RequiredArgsConstructor
public class RequestController {
    //자동 주입
    private final RequestService requestService; 

    @GetMapping("/")
    public String estimate() {
      return "redirect:/smash/request/listTest";
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
        return "redirect:/smash/request/listTest"; // 저장 후 홈 또는 목록으로 이동

    @GetMapping
    public List<RequestListDTO> getAllRequests() {
        return requestService.getRequestList();
    }

    @PostMapping
    public Request createRequest(@RequestBody Request request) {
        return requestService.save(request);
    }

    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Integer id) {
        return requestService.getById(id);
    }
}
