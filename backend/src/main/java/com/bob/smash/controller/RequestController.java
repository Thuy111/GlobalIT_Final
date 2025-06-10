package com.bob.smash.controller;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/smash/request")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;

    @GetMapping("/")
    public String estimate() {
    return "redirect:/smash/request/listTest";
  }

    // 의뢰서 목록 보기
    @GetMapping("/listTest")
    public String list(Model model) {
        List<RequestDTO> result = requestService.getList();
        model.addAttribute("result", result);
        // return "redirect:/smash/request/listTest"; 
        return "smash/request/listTest";
    }

    // 의뢰서 작성 폼 보기
    @GetMapping("/register")
    public String register() {
        return "/smash/request/register";
    }

    // 의뢰서 등록 처리
    @PostMapping("/register")
    public String register(@ModelAttribute RequestDTO requestDTO, Model model) {
         log.info("📝 Received RequestDTO: {}", requestDTO); // debug DTO


        Integer savedIdx = requestService.register(requestDTO, null);

         log.info("✅ Saved Request with idx: {}", savedIdx); // debug DB 저장 결과

        model.addAttribute("msg", savedIdx);
        return "redirect:/smash/request/listTest";
    }

    //  의뢰서 상세 보기
    // @GetMapping("/read")
    // public String read(@RequestParam("idx") Integer idx, Model model) {
    //     RequestDTO dto = requestService.get(idx);
    //     model.addAttribute("dto", dto);
    //     return "request/read";  // templates/request/read.html
    // }
}