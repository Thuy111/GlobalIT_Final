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
import org.springframework.web.bind.annotation.RequestBody;


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
  public String estimateRegister(EstimateDTO dto, RedirectAttributes rttr) {
    log.info("견적서 등록 요청: {}", dto);
    Integer idx = service.register(dto);
    rttr.addFlashAttribute("message", "견적서가 등록되었습니다. (ID: " + idx + ")");
    return "redirect:/smash/estimate/list";
  }
  
  // 반납 상태(isReturn) 수정
  @PostMapping("/return")
  public String estimateReturn(@RequestParam("idx") Integer idx,
                               @RequestParam("isReturn") Boolean isReturn,
                               RedirectAttributes rttr) {
    log.info("반납 현황 수정 요청: idx={}, isReturn={}", idx, isReturn);
    
    EstimateDTO dto = service.get(idx);
    dto.setIsReturn(isReturn);
    
    service.returnStatus(dto);
    
    rttr.addFlashAttribute("message", "반납 현황이 수정되었습니다. (ID: " + idx + ")");
    return "redirect:/smash/estimate/list";
  }

  // 수정
  @GetMapping("/update")
  public void update(@RequestParam("idx") Integer idx, Model model) {
    EstimateDTO dto = service.get(idx);
    model.addAttribute("dto", dto);
  }
  @PostMapping("/modify")
  public String modify() {
    return "redirect:/smash/estimate/list";
  }
  
  // 낙찰 상태(isSelected) 수정
  // @PostMapping("/status")
  // public String estimateStatus(@RequestParam("idx") Integer idx,
  //                              @RequestParam("isSelected") Byte isSelected,
  //                               RedirectAttributes rttr) {
  //   log.info("견적서 상태 수정 요청: idx={}, isReturn={}, isSelected={}", idx, isSelected);
    
  //   EstimateDTO dto = service.get(idx);
  //   dto.setIsSelected(isSelected);
    
  //   // service.modify(dto);
    
  //   rttr.addFlashAttribute("message", "견적서 낙찰 상태가 수정되었습니다. (ID: " + idx + ")");
  //   return "redirect:/smash/estimate/list";
  // }
}