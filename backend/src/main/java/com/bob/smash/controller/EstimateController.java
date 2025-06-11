package com.bob.smash.controller;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.service.EstimateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/estimate")
public class EstimateController {
  private final EstimateService service;  

  @GetMapping("/")
  public String estimate() {
    return "redirect:/smash/estimate/list";
  }

  // 목록
  @GetMapping("/list")
  public void list(Model model) {
    model.addAttribute("result", service.getList());
  }

  // 등록
  @GetMapping("/register")
  public void register() {}
  @PostMapping("/register")
  public String registerEstimate(EstimateDTO dto, RedirectAttributes rttr) {
    log.info("견적서 등록 요청: {}", dto);
    Integer idx = service.register(dto);
    rttr.addFlashAttribute("message", "견적서가 등록되었습니다. (ID: " + idx + ")");
    return "redirect:/smash/estimate/list";
  }
  
  // 조회
  @GetMapping("/read")
  public void read(@RequestParam("idx") Integer idx, Model model) {
    EstimateDTO dto = service.get(idx);
    model.addAttribute("dto", dto);
  }
}