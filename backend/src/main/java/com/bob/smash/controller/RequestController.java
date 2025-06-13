package com.bob.smash.controller;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/smash/request")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;
    private final MemberRepository memberRepository; //thêm

    @GetMapping("/")
    public String estimate() {
    return "redirect:/smash/request/listTest";
  }

    // 의뢰서 목록 보기//////////////////////////
    @GetMapping("/listTest")
    public String list(Model model) {
        List<RequestDTO> result = requestService.getList();
        model.addAttribute("result", result); 
        return "smash/request/listTest";
    }

    // 의뢰서 작성 폼 보기////////////////////////////
    @GetMapping("/register")
    public String register() {
        return "/smash/request/register";
    }

    // 의뢰서 등록 처리
    @PostMapping("/register")
   
    public String register(@ModelAttribute RequestDTO requestDTO,                        
                         @RequestParam("detailAddress") String detailAddress, //detail 주소
                         @AuthenticationPrincipal OAuth2User oauth2User,
                         Model model) {

    String email = oauth2User.getAttribute("email");
    log.info(" Logged in email: {}", email);

    Member member = memberRepository.findByEmailId(email)
                      .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다"));


    // 메인주소와 detail 주소 묶어서 DB에 저장
    String fullAddress = requestDTO.getUseRegion() + " " + detailAddress;
    requestDTO.setUseRegion(fullAddress);                  

    Integer savedIdx = requestService.register(requestDTO, member);
    model.addAttribute("msg", savedIdx);

    return "redirect:/smash/request/listTest";
    }

    // ⭐ 추가
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPagedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search){

        return ResponseEntity.ok(requestService.getPagedRequestList(page, size, search));
    }



    //  의뢰서 상세 보기//////////////////////////
    @GetMapping("/detail")
    public String detail(@RequestParam("idx") Integer idx, Model model) {
        RequestDTO dto = requestService.get(idx);
        model.addAttribute("dto", dto);
        return "/smash/request/detail";  
    }
}