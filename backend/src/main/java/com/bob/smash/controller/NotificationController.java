package com.bob.smash.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.dto.NotificationMappingDTO;
import com.bob.smash.service.NotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/alarm")
public class NotificationController {
  private final NotificationService service;

  @GetMapping({"","/"})
  public String alarm() {
    return "redirect:/smash/alarm/list";
  }
  
  //목록
  @GetMapping("/list")
  public ResponseEntity<List<NotificationMappingDTO>> list(HttpSession session) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    List<NotificationMappingDTO> result = service.getNotificationByMember(currentUser.getEmailId());
    return ResponseEntity.ok(result);
  }

  // 알림 읽음 처리
  @ResponseBody
  @PostMapping("/read")
  public String alarmRead(@RequestParam("idx") Integer idx,
                          HttpSession session,
                          RedirectAttributes rttr) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    service.readNotification(currentUser.getEmailId(), idx);
    return "ok";
  }
}