package com.bob.smash.controller;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.service.EstimateService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/estimate")
public class EstimateController {
  private final EstimateService service;  

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
    Integer idx = service.register(dto);
    rttr.addFlashAttribute("message", "견적서가 등록되었습니다. (ID: " + idx + ")");
    return "redirect:/estimate/list";
  }
  // 조회
  @GetMapping("/read")
  public void read(@RequestParam("idx") Integer idx, Model model) {
    EstimateDTO dto = service.get(idx);
    model.addAttribute("dto", dto);
  }
}