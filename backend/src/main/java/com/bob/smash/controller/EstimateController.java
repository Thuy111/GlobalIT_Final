package com.bob.smash.controller;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.service.EstimateService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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

  // 업체가 쓴 견적서 목록(이미지 포함)
  @GetMapping("/list")
  public void list(@RequestParam("partnerBno") String partnerBno, Model model) {
    model.addAttribute("result", service.getListByPartnerBno(partnerBno));
    model.addAttribute("title", "견적서 목록");
  }
  // 내가 쓴 견적서 목록(이미지 포함)
  @GetMapping("/mylist")
  public String myList(HttpSession session, Model model) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    // currentUser 또는 bno가 null이면 홈으로 리다이렉트
    if (currentUser == null || currentUser.getBno() == null) {
      return "redirect:/smash";
    }
    model.addAttribute("result", service.getListByPartnerBno(currentUser.getBno()));
    model.addAttribute("title", "견적서 목록");
    return "redirect:/smash/estimate/list?partnerBno=" + currentUser.getBno();
  }

  // 등록(이미지 포함)
  @GetMapping("/register")
  public void register(@RequestParam("requestIdx") Integer requestIdx, Model model) {
    model.addAttribute("requestIdx", requestIdx);
    model.addAttribute("title", "견적서 등록");
  }
  @PostMapping("/register")
  public String estimateRegister(EstimateDTO dto,
                                 @RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
                                 RedirectAttributes rttr)  {
    Integer idx;
    if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
      idx = service.registerWithImage(dto, imageFiles);
    } else {
      idx = service.register(dto);
    }
    rttr.addFlashAttribute("message", "견적서가 등록되었습니다. (ID: " + idx + ")");
    return "redirect:/smash/request/detail/" + dto.getRequestIdx();
  }

  // 수정
  @GetMapping("/update")
  public void update(@RequestParam("idx") Integer idx, Model model) {
    EstimateDTO dto = service.getWithImage(idx);
    model.addAttribute("dto", dto);
    model.addAttribute("title", "견적서 수정");
  }
  @PostMapping("/modify")
  public String modify(EstimateDTO dto, 
                       @RequestParam(value = "delete_images", required = false) String deleteImages,
                       @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
    List<Integer> deleteImageIdxList = (deleteImages == null || deleteImages.isEmpty())
      ? Collections.emptyList() : Arrays.stream(deleteImages.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    service.modifyWithImage(dto, deleteImageIdxList, imageFiles);
    return "redirect:/smash/request/detail/" + dto.getRequestIdx();
  }

  // 반납 상태(isReturn) 수정
  @PostMapping("/return")
  public String estimateReturn(@RequestParam("idx") Integer idx,
                               @RequestParam("isReturn") Boolean isReturn,
                               RedirectAttributes rttr) {
    // log.info("반납 현황 수정 요청: idx={}, isReturn={}", idx, isReturn);
    EstimateDTO dto = service.get(idx);
    dto.setIsReturn(isReturn);
    service.changeReturnStatus(dto);
    rttr.addFlashAttribute("message", "반납 현황이 수정되었습니다. (ID: " + idx + ")");
    return "redirect:/smash/request/detail/" + dto.getRequestIdx();
  }

  // 삭제
  @PostMapping("/delete")
  public String delete(@RequestParam("idx") Integer idx, RedirectAttributes rttr) {
    // log.info("견적서 삭제 요청: idx={}", idx);
    Integer requestIdx = service.get(idx).getRequestIdx();
    service.deleteWithImage(idx);
    rttr.addFlashAttribute("message", "견적서가 삭제되었습니다. (ID: " + idx + ")");
    return "redirect:/smash/request/detail/" + requestIdx;
  }
}