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

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.service.NotificationService;

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
  
  // 목록
  @GetMapping("/list")
  public ResponseEntity<List<NotificationDTO>> list() {
    log.info(service.getList());
    return ResponseEntity.ok(service.getList());
  }

  // 알림 읽음 처리
  @ResponseBody
  @PostMapping("/read")
  public String alarmRead(@RequestParam("idx") Integer idx,
                          @RequestParam("isRead") Boolean isRead,
                          RedirectAttributes rttr) {
    // log.info("알림 읽음 처리 요청: idx={}, isRead={}", idx, isRead);
    service.markAsRead(idx);
    return "ok";
  }
}